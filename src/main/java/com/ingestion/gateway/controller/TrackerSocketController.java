package com.ingestion.gateway.controller;

import com.ingestion.gateway.service.TrackerIngestionService;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.Vertx;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import io.vertx.core.parsetools.RecordParser;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.Handler;

// 1. Tambahkan import untuk Consumer
import java.util.function.Consumer;

@ApplicationScoped
public class TrackerSocketController {

    private static final Logger LOG = Logger.getLogger(TrackerSocketController.class);

    private static final int TELEMETRY_PORT = 8081;
    private static final int STREAM_PORT = 1078;

    @Inject
    TrackerIngestionService ingestionService;

    public void onStart(@Observes StartupEvent ev, Vertx vertx) {
        startTelemetryTcpListener(vertx);
        startStreamTcpListener(vertx);
        startUdpListener(vertx);
    }

    private void startTelemetryTcpListener(Vertx vertx) {
        NetServerOptions options = new NetServerOptions().setPort(TELEMETRY_PORT).setHost("0.0.0.0");
        NetServer server = vertx.createNetServer(options);

        server.connectHandler(socket -> {
            String remoteAddress = socket.remoteAddress().toString();
            LOG.info("Koneksi TCP Telemetri Baru: " + remoteAddress);

            // 2. Buat Consumer untuk TCP
            // Fungsi ini akan mengeksekusi penulisan byte kembali ke MDVR
            Consumer<byte[]> tcpReplier = responseBytes -> {
                if (responseBytes != null && responseBytes.length > 0) {
                    socket.write(Buffer.buffer(responseBytes))
                            .onSuccess(v -> LOG.debugf("Balasan terkirim ke %s", remoteAddress))
                            .onFailure(e -> LOG.error("Gagal mengirim balasan ke " + remoteAddress, e));
                }
            };

            RecordParser parser = RecordParser.newDelimited(Buffer.buffer(new byte[]{0x7E}), buffer -> {
                if (buffer.length() == 0) return;

                Buffer frame = Buffer.buffer().appendByte((byte) 0x7E).appendBuffer(buffer).appendByte((byte) 0x7E);

                vertx.executeBlocking(promise -> {
                    try {
                        // 3. Sisipkan tcpReplier sebagai parameter ke-4
                        ingestionService.processRawMessage(remoteAddress, "TCP-TELEMETRY", frame.getBytes(), tcpReplier);
                        promise.complete();
                    } catch (Exception e) {
                        promise.fail(e);
                    }
                }, res -> {
                    if (res.failed()) {
                        LOG.error("Gagal memproses pesan Telemetri TCP: " + res.cause().getMessage(), res.cause());
                    }
                });
            });

            socket.handler(parser);
            socket.closeHandler(v -> LOG.info("Koneksi TCP Telemetri Terputus: " + remoteAddress));
        });

        server.listen().onSuccess(s -> LOG.info("TCP Telemetri Listener (JTT808) berjalan di port " + TELEMETRY_PORT))
                .onFailure(Throwable::printStackTrace);
    }

    private void startStreamTcpListener(Vertx vertx) {
        NetServerOptions options = new NetServerOptions().setPort(STREAM_PORT).setHost("0.0.0.0");
        NetServer server = vertx.createNetServer(options);

        server.connectHandler(socket -> {
            String remoteAddress = socket.remoteAddress().toString();
            LOG.info("Koneksi TCP Stream Baru: " + remoteAddress);

            // Handler khusus untuk menampung potongan TCP JTT1078
            socket.handler(new Handler<Buffer>() {
                // Buffer sementara untuk menyambung potongan TCP
                Buffer accumulator = Buffer.buffer();

                @Override
                public void handle(Buffer chunk) {
                    // 1. Tambahkan potongan baru ke penampung
                    accumulator.appendBuffer(chunk);

                    // 2. Cek apakah panjang data sudah memenuhi minimal 1 Header (30 byte)
                    while (accumulator.length() >= 30) {

                        // Opsional: Cek Sync Word JTT1078 (0x30 0x31 0x63 0x64) di 4 byte pertama
                        // Jika tidak cocok, mungkin perlu membuang byte pertama sampai ketemu header.
                        // Asumsi saat ini stream mulus.

                        // 3. Baca Data Body Length (Byte ke-28 dan 29 sebagai Unsigned Short)
                        int bodyLength = accumulator.getUnsignedShort(28);

                        // 4. Hitung total panjang 1 frame utuh
                        int totalFrameLength = 30 + bodyLength;

                        // 5. Cek apakah seluruh data frame sudah mendarat di penampung
                        if (accumulator.length() >= totalFrameLength) {
                            // Frame sudah lengkap! Potong dari accumulator
                            byte[] completeFrame = accumulator.getBytes(0, totalFrameLength);

                            // Hapus frame yang sudah diproses dari accumulator
                            accumulator = accumulator.getBuffer(totalFrameLength, accumulator.length());

                            // 6. Teruskan frame UTUH ke Service & Codec
                            vertx.executeBlocking(promise -> {
                                try {
                                    Consumer<byte[]> streamReplier = bytes -> {};
                                    ingestionService.processRawMessage(remoteAddress, "TCP-STREAM", completeFrame, streamReplier);
                                    promise.complete();
                                } catch (Exception e) {
                                    promise.fail(e);
                                }
                            }, res -> {
                                if (res.failed()) {
                                    LOG.error("Gagal memproses frame JTT1078: " + res.cause().getMessage());
                                }
                            });

                        } else {
                            // Frame belum lengkap (masih terpotong).
                            // Hentikan loop dan tunggu sisa byte datang di event jaringan berikutnya.
                            break;
                        }
                    }
                }
            });

            socket.closeHandler(v -> LOG.info("Koneksi TCP Stream Terputus: " + remoteAddress));
        });

        server.listen().onSuccess(s -> LOG.info("TCP Stream Listener (JTT1078) berjalan di port " + STREAM_PORT))
                .onFailure(Throwable::printStackTrace);
    }

    private void startUdpListener(Vertx vertx) {
        DatagramSocket socket = vertx.createDatagramSocket(new DatagramSocketOptions());

        socket.listen(TELEMETRY_PORT, "0.0.0.0").onSuccess(s -> {
            LOG.info("UDP Listener berjalan di port " + TELEMETRY_PORT);

            s.handler(packet -> {
                String remoteAddress = packet.sender().toString();

                // 4. Buat Consumer untuk UDP
                // Pada UDP, kita tidak memiliki "socket koneksi", melainkan menembak balik ke IP dan Port pengirim.
                Consumer<byte[]> udpReplier = responseBytes -> {
                    if (responseBytes != null && responseBytes.length > 0) {
                        socket.send(Buffer.buffer(responseBytes), packet.sender().port(), packet.sender().host())
                                .onSuccess(v -> LOG.debugf("Balasan UDP terkirim ke %s", remoteAddress))
                                .onFailure(e -> LOG.error("Gagal mengirim balasan UDP ke " + remoteAddress, e));
                    }
                };

                vertx.executeBlocking(promise -> {
                    try {
                        // 5. Sisipkan udpReplier
                        ingestionService.processRawMessage(remoteAddress, "UDP", packet.data().getBytes(), udpReplier);
                        promise.complete();
                    } catch (Exception e) {
                        promise.fail(e);
                    }
                }, res -> {
                    if (res.failed()) {
                        LOG.error("Gagal memproses pesan UDP: " + res.cause().getMessage(), res.cause());
                    }
                });
            });
        }).onFailure(Throwable::printStackTrace);
    }
}