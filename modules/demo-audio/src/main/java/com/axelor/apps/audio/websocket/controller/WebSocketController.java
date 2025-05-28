package com.axelor.apps.audio.websocket.controller;

import com.axelor.apps.audio.websocket.dto.CommandDto;
import com.axelor.apps.audio.websocket.service.WebSocketService;
import com.google.inject.Inject;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("api/ws/command")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WebSocketController {

    private final WebSocketService webSocketService;

    @Inject
    public WebSocketController(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    @POST
    @Path("/send")
    public Response sendCommand(CommandDto commandDto) {
        try {
            webSocketService.sendTo(commandDto);
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
        return Response.ok(commandDto).build();
    }
}
