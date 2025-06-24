package com.axelor.apps.audio.service.impl;

import com.axelor.apps.audio.db.CustomsOffice;
import com.axelor.apps.audio.db.SoundTask;
import com.axelor.apps.audio.db.repo.SoundTaskRepository;
import com.axelor.apps.audio.service.SoundExecutorService;
import com.axelor.apps.audio.service.SoundSenderService;
import com.axelor.apps.audio.service.SoundTaskService;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class SoundExecutorServiceImpl implements SoundExecutorService {
    private static final Logger log = LoggerFactory.getLogger(SoundExecutorServiceImpl.class);
    private final SoundTaskRepository soundTaskRepository;
    private final SoundTaskService soundTaskService;
    private final SoundSenderService soundSenderService;

    @Inject
    public SoundExecutorServiceImpl(SoundTaskRepository soundTaskRepository, SoundTaskService soundTaskService, SoundSenderService soundSenderService) {
        this.soundTaskRepository = soundTaskRepository;
        this.soundTaskService = soundTaskService;
        this.soundSenderService = soundSenderService;
    }

    @Override
    public void play(Long soundTaskId) {
        SoundTask soundTask = soundTaskRepository.find(soundTaskId);
        if (soundTask == null) return;
        if (!soundTask.getIsActive()) return;

        sendSoundRequest(soundTask);

        if (soundTask.getOneTimeTask()) {
            try {
                soundTask = soundTaskService.disable(soundTask);
            } catch (SchedulerException ignore) {}
        }
    }

    private void sendSoundRequest(SoundTask soundTask) {
        if (soundTask == null) return;
        if (!soundTask.getIsActive()) return;
        if (soundTask.getSoundFile() == null) return;
        if (soundTask.getCustomsOffices() == null || soundTask.getCustomsOffices().isEmpty()) return;
        Set<CustomsOffice> customsOffices = soundTask.getCustomsOffices();
        MetaFile soundFile = soundTask.getSoundFile();

        for (CustomsOffice office : customsOffices) {
                try {
                    soundSenderService.send(soundFile.getId(), office);
                } catch (Exception e) {
                    log.debug("Failed to send sound request", e);
                }
        }
    }

}
