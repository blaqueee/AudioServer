package com.axelor.apps.audio.service.impl;

import com.axelor.apps.audio.db.CustomsOffice;
import com.axelor.apps.audio.db.repo.CustomsOfficeRepo;
import com.axelor.apps.audio.dto.OnlineClientsDto;
import com.axelor.apps.audio.service.OnlineClientsService;
import com.axelor.apps.audio.websocket.SessionStorage;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class OnlineClientsServiceImpl implements OnlineClientsService {

    private final SessionStorage sessionStorage;
    private final CustomsOfficeRepo customsOfficeRepo;

    @Inject
    public OnlineClientsServiceImpl(SessionStorage sessionStorage, CustomsOfficeRepo customsOfficeRepo) {
        this.sessionStorage = sessionStorage;
        this.customsOfficeRepo = customsOfficeRepo;
    }

    @Override
    public List<OnlineClientsDto> getWSClients() {
        List<Long> clientIDes = sessionStorage.getClientIDes();
        return mapToDto(clientIDes);
    }

    @Override
    public List<OnlineClientsDto> getTcpClients() {
        return List.of();
    }


    private List<OnlineClientsDto> mapToDto(List<Long> clientIDes) {
        List<OnlineClientsDto> clientsDtos = new ArrayList<>();

        for (Long clientID : clientIDes) {
            OnlineClientsDto clientDto = new OnlineClientsDto();
            CustomsOffice customsOffice = customsOfficeRepo.find(clientID);
            clientDto.setCustomOffice(customsOffice.getName());
            clientDto.setStatus("Online");
            clientsDtos.add(clientDto);
        }
        return clientsDtos;
    }
}
