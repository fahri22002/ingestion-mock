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

            socket.handler(new Handler<Buffer>() {
                Buffer accumulator = Buffer.buffer();

                @Override
                public void handle(Buffer chunk) {
                    accumulator.appendBuffer(chunk);

                    while (accumulator.length() >= 30) {
                        // 1. Cari Magic Word: 0x30 0x31 0x63 0x64
                        int magicIdx = -1;
                        for (int i = 0; i <= accumulator.length() - 4; i++) {
                            if (accumulator.getByte(i) == 0x30 && accumulator.getByte(i + 1) == 0x31 &&
                                    accumulator.getByte(i + 2) == 0x63 && accumulator.getByte(i + 3) == 0x64) {
                                magicIdx = i;
                                break;
                            }
                        }

                        // Jika tidak ketemu, simpan 3 byte terakhir ke dalam BUFFER BARU
                        if (magicIdx == -1) {
                            byte[] sisa = accumulator.getBytes(accumulator.length() - 3, accumulator.length());
                            accumulator = Buffer.buffer(sisa);
                            break;
                        }

                        // Buang data sampah sebelum Magic Word (Pindahkan sisa ke BUFFER BARU)
                        if (magicIdx > 0) {
                            byte[] sisa = accumulator.getBytes(magicIdx, accumulator.length());
                            accumulator = Buffer.buffer(sisa);
                        }

                        // 2. Baca Header JTT1078
                        if (accumulator.length() >= 30) {
                            int dataType = (accumulator.getByte(15) >> 4) & 0x0F;

                            int headerLength = 30; // Tipe 0,1,2 (Video)
                            if (dataType == 4) headerLength = 18; // Data transparan
                            else if (dataType == 3) headerLength = 26; // Audio

                            if (accumulator.length() >= headerLength) {
                                int bodyLengthOffset = headerLength - 2;
                                int bodyLength = accumulator.getUnsignedShort(bodyLengthOffset);
                                int totalFrameLength = headerLength + bodyLength;

                                // 3. Jika frame sudah lengkap, potong dan kirim ke Service
                                if (accumulator.length() >= totalFrameLength) {
                                    byte[] completeFrame = accumulator.getBytes(0, totalFrameLength);

                                    // PERBAIKAN: Reset accumulator dengan aman
                                    if (accumulator.length() == totalFrameLength) {
                                        // Jika data pas habis, buat buffer kosong baru
                                        accumulator = Buffer.buffer();
                                    } else {
                                        // Jika ada sisa byte dari frame berikutnya, bungkus di buffer baru
                                        byte[] sisaLagi = accumulator.getBytes(totalFrameLength, accumulator.length());
                                        accumulator = Buffer.buffer(sisaLagi);
                                    }

                                    vertx.executeBlocking(promise -> {
                                        try {
                                            Consumer<byte[]> streamReplier = bytes -> {
                                                if (bytes != null && bytes.length > 0) socket.write(Buffer.buffer(bytes));
                                            };
                                            ingestionService.processRawMessage(remoteAddress, "TCP-STREAM", completeFrame, streamReplier);
                                            promise.complete();
                                        } catch (Exception e) {
                                            promise.fail(e);
                                        }
                                    }, res -> {
                                        if (res.failed()) LOG.error("Gagal proses frame: " + res.cause().getMessage());
                                    });

                                } else {
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                    }
                }
            });

            socket.closeHandler(v -> LOG.info("Koneksi TCP Stream Terputus: " + remoteAddress));
        });

        server.listen().onSuccess(s -> LOG.info("TCP Stream Listener berjalan di port " + STREAM_PORT))
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