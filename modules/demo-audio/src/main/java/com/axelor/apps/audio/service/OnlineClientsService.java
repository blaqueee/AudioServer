package com.axelor.apps.audio.service;

import com.axelor.apps.audio.dto.OnlineClientsDto;

import java.util.List;

public interface OnlineClientsService {

    List<OnlineClientsDto> getWSClients();
    List<OnlineClientsDto> getTcpClients();

}
