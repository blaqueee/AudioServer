package com.axelor.apps.audio.service.impl;

import com.axelor.apps.audio.db.CustomsOffice;
import com.axelor.apps.audio.helper.JsonConvert;
import com.axelor.apps.audio.service.SoundSenderService;
import com.axelor.apps.audio.websocket.SessionStorage;
import com.axelor.apps.audio.websocket.dto.CommandDto;
import com.google.inject.Inject;

import java.io.File;

import static com.axelor.apps.audio.websocket.dto.CommandDto.COMMAND_PLAY;

public class SoundSenderServiceImpl implements SoundSenderService {

    private final SessionStorage sessionStorage;

    @Inject
    public SoundSenderServiceImpl(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void send(File soundFile, CustomsOffice customsOffice) throws Exception {
        if (soundFile == null || customsOffice == null) return;
        CommandDto commandDto =
                CommandDto.toCommandDto(COMMAND_PLAY, soundFile.getAbsolutePath());
        sessionStorage
                .sendTo(customsOffice.getId(), JsonConvert.toJson(commandDto));
    }
}