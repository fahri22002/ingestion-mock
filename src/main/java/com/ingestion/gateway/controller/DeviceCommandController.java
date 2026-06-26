package com.ingestion.gateway.controller;

import com.ingestion.gateway.service.DeviceCommandService;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Consumes;
import com.ingestion.gateway.dto.SetParametersRequestDto;

@Path("/api/device")
public class DeviceCommandController {

    private static final Logger LOG = Logger.getLogger(DeviceCommandController.class);

    @Inject
    DeviceCommandService commandService;

    @GET
    @Path("/{imei}/stream/start")
    @Produces(MediaType.APPLICATION_JSON)
    public Response startStream(@PathParam("imei") String imei) {

        LOG.infof("Menerima request HTTP untuk membuka stream IMEI: %s", imei);

        // Parameter IP Server (Laptop Anda) dan Port JTT1078 yang sudah kita siapkan
        String serverIp = "172.16.17.116";
        int streamPort = 1078;
        int cameraChannel = 1; // Default kamera 1

        boolean isSent = commandService.sendStartStreamCommand(imei, serverIp, streamPort, cameraChannel);

        if (isSent) {
            return Response.ok("{\"status\":\"success\", \"message\":\"Perintah stream berhasil dikirim ke perangkat.\"}").build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"status\":\"error\", \"message\":\"Perangkat offline atau terjadi kesalahan internal.\"}")
                    .build();
        }
    }

    @POST
    @Path("/{imei}/parameters")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response setParameters(@PathParam("imei") String imei, SetParametersRequestDto requestBody) {

        LOG.infof("Menerima request HTTP POST untuk set parameter IMEI: %s", imei);

        boolean isSent = commandService.sendSetParametersCommand(imei, requestBody);

        if (isSent) {
            return Response.ok("{\"status\":\"success\", \"message\":\"Perintah set parameter berhasil dikirim ke perangkat.\"}").build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"status\":\"error\", \"message\":\"Perangkat offline atau terjadi kesalahan internal.\"}")
                    .build();
        }
    }
}