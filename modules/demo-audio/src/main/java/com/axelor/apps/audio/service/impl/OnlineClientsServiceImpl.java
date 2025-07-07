package com.axelor.apps.audio.service.impl;

import com.axelor.apps.audio.db.CustomsOffice;
import com.axelor.apps.audio.db.repo.CustomsOfficeRepo;
import com.axelor.apps.audio.dto.OnlineClientsDto;
import com.axelor.apps.audio.service.OnlineClientsService;
import com.axelor.apps.audio.tcp.TcpSessionStorage;
import com.axelor.apps.audio.websocket.SessionStorage;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OnlineClientsServiceImpl implements OnlineClientsService {

    private final SessionStorage sessionStorage;
    private final CustomsOfficeRepo customsOfficeRepo;
    private final TcpSessionStorage tcpSessionStorage;

    @Inject
    public OnlineClientsServiceImpl(SessionStorage sessionStorage, CustomsOfficeRepo customsOfficeRepo, TcpSessionStorage tcpSessionStorage) {
        this.sessionStorage = sessionStorage;
        this.customsOfficeRepo = customsOfficeRepo;
        this.tcpSessionStorage = tcpSessionStorage;
    }

    @Override
    public List<OnlineClientsDto> getWSClients() {
        List<Long> clientIDes = sessionStorage.getClientIds();
        return mapToDto(clientIDes);
    }

    @Override
    public List<OnlineClientsDto> getTcpClients() {
        List<Long> clientIds = tcpSessionStorage.getClientIds().stream()
                .map(Long::parseLong)
                .collect(Collectors.toList());

        return mapToDto(clientIds);
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
