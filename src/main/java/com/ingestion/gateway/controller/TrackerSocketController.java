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

@ApplicationScoped
public class TrackerSocketController {

    private static final Logger LOG = Logger.getLogger(TrackerSocketController.class);
    private static final int PORT = 8081;

    @Inject
    TrackerIngestionService ingestionService;

    public void onStart(@Observes StartupEvent ev, Vertx vertx) {
        startTcpListener(vertx);
        startUdpListener(vertx);
    }

    private void startTcpListener(Vertx vertx) {
        NetServerOptions options = new NetServerOptions().setPort(PORT).setHost("0.0.0.0");
        NetServer server = vertx.createNetServer(options);

        server.connectHandler(socket -> {
            String remoteAddress = socket.remoteAddress().toString();
            LOG.info("Koneksi TCP Baru: " + remoteAddress);

            socket.handler(buffer -> {
                // Gunakan executeBlocking agar Redis tidak memblokir Event Loop
                vertx.executeBlocking(promise -> {
                    try {
                        ingestionService.processRawMessage(remoteAddress, "TCP", buffer.getBytes());
                        promise.complete();
                    } catch (Exception e) {
                        promise.fail(e);
                    }
                }, res -> {
                    if (res.failed()) {
                        LOG.error("Gagal memproses pesan TCP: " + res.cause().getMessage(), res.cause());
                    }
                });
            });

            socket.closeHandler(v -> LOG.info("Koneksi TCP Terputus: " + remoteAddress));
        });

        server.listen().onSuccess(s -> LOG.info("TCP Listener berjalan di port " + PORT))
                .onFailure(Throwable::printStackTrace);
    }

    private void startUdpListener(Vertx vertx) {
        DatagramSocket socket = vertx.createDatagramSocket(new DatagramSocketOptions());

        socket.listen(PORT, "0.0.0.0").onSuccess(s -> {
            LOG.info("UDP Listener berjalan di port " + PORT);

            s.handler(packet -> {
                String remoteAddress = packet.sender().toString();

                // Gunakan executeBlocking agar Redis tidak memblokir Event Loop
                vertx.executeBlocking(promise -> {
                    try {
                        ingestionService.processRawMessage(remoteAddress, "UDP", packet.data().getBytes());
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