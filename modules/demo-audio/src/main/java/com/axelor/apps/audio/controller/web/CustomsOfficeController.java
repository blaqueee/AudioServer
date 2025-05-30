package com.axelor.apps.audio.controller.web;

import com.axelor.apps.audio.service.CustomsOfficeService;
import com.google.inject.Inject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/public/customs-office")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CustomsOfficeController {

    private final CustomsOfficeService customsOfficeService;

    @Inject
    public CustomsOfficeController(CustomsOfficeService customsOfficeService) {
        this.customsOfficeService = customsOfficeService;
    }

    @GET
    @Path("/parents")
    public Response getParents() {
        return Response.ok(customsOfficeService.getAllParents()).build();
    }

    @GET
    @Path("/by/parent")
    public Response getByParentId(@QueryParam("parentId") Long parentId) {
        return Response.ok(customsOfficeService.getAllByParentId(parentId)).build();
    }

}
