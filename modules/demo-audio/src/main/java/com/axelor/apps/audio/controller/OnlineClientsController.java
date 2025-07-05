package com.axelor.apps.audio.controller;

import com.axelor.apps.audio.dto.OnlineClientsDto;
import com.axelor.apps.audio.service.OnlineClientsService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

import javax.inject.Inject;
import java.util.List;

public class OnlineClientsController {

    private final OnlineClientsService onlineClientsService;

    @Inject
    public OnlineClientsController(OnlineClientsService onlineClientsService) {
        this.onlineClientsService = onlineClientsService;
    }

    public void getWsClients(ActionRequest request, ActionResponse response) {
        List<OnlineClientsDto> wsClients = onlineClientsService.getWSClients();

        response.setValue("$onlineClients", wsClients);
    }

}
