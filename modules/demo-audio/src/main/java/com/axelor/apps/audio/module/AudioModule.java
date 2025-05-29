package com.axelor.apps.audio.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.audio.service.SoundExecutorService;
import com.axelor.apps.audio.service.SoundSenderService;
import com.axelor.apps.audio.service.SoundService;
import com.axelor.apps.audio.service.SoundTaskService;
import com.axelor.apps.audio.service.impl.SoundExecutorServiceImpl;
import com.axelor.apps.audio.service.impl.SoundSenderServiceImpl;
import com.axelor.apps.audio.service.impl.SoundServiceImpl;
import com.axelor.apps.audio.service.impl.SoundTaskServiceImpl;
import com.axelor.apps.audio.websocket.SessionStorage;

public class AudioModule extends AxelorModule {

    @Override
    protected void configure() {
        bind(SoundExecutorService.class).to(SoundExecutorServiceImpl.class);
        bind(SoundTaskService.class).to(SoundTaskServiceImpl.class);
        bind(SoundSenderService.class).to(SoundSenderServiceImpl.class);
        bind(SoundService.class).to(SoundServiceImpl.class);
        bind(SessionStorage.class).asEagerSingleton();
    }

}
