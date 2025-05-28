package com.axelor.apps.audio.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.audio.service.SoundExecutorService;
import com.axelor.apps.audio.service.SoundSenderService;
import com.axelor.apps.audio.service.SoundTaskService;
import com.axelor.apps.audio.service.impl.SoundExecutorServiceImpl;
import com.axelor.apps.audio.service.impl.SoundSenderServiceImpl;
import com.axelor.apps.audio.service.impl.SoundTaskServiceImpl;
import com.axelor.apps.audio.websocket.service.WebSocketService;
import com.axelor.apps.audio.websocket.service.impl.WebSocketServiceImpl;

public class AudioModule extends AxelorModule {

    @Override
    protected void configure() {
        bind(SoundExecutorService.class).to(SoundExecutorServiceImpl.class);
        bind(SoundTaskService.class).to(SoundTaskServiceImpl.class);
        bind(SoundSenderService.class).to(SoundSenderServiceImpl.class);
        bind(WebSocketService.class).to(WebSocketServiceImpl.class);
    }

}
