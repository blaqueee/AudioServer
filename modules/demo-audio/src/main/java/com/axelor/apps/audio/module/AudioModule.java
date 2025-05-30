package com.axelor.apps.audio.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.audio.db.repo.CustomsOfficeRepo;
import com.axelor.apps.audio.db.repo.CustomsOfficeRepository;
import com.axelor.apps.audio.service.*;
import com.axelor.apps.audio.service.impl.*;
import com.axelor.apps.audio.websocket.SessionStorage;
import com.axelor.apps.audio.websocket.WebSocketServer;
import com.axelor.web.socket.WebSocketEndpoint;

public class AudioModule extends AxelorModule {

    @Override
    protected void configure() {
        bind(SoundExecutorService.class).to(SoundExecutorServiceImpl.class);
        bind(SoundTaskService.class).to(SoundTaskServiceImpl.class);
        bind(SoundSenderService.class).to(SoundSenderServiceImpl.class);
        bind(SoundService.class).to(SoundServiceImpl.class);
        bind(SearchService.class).to(SearchServiceImpl.class);
        bind(SessionStorage.class).asEagerSingleton();

        this.bind(WebSocketEndpoint.class).asEagerSingleton();
        bind(WebSocketServer.class).asEagerSingleton();
        bind(CustomsOfficeRepository.class).to(CustomsOfficeRepo.class);
        bind(CustomsOfficeService.class).to(CustomsOfficeServiceImpl.class);
    }

}
