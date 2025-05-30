package com.axelor.apps.audio.controller.web;

import com.axelor.apps.audio.service.SoundService;
import com.google.inject.Inject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;

@Path("/public/download")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SoundController {

    private final SoundService service;

    @Inject
    public SoundController(SoundService service) {
        this.service = service;
    }

    @GET
    @Path("/")
    public Response getSound(@QueryParam("id") String id) {
        try {
            File soundFile = service.getSoundFile(id);
            return Response.ok(soundFile, "audio/mpeg")
                    .header("Content-Disposition", "attachment; filename=\"" + soundFile.getName() + "\"")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
