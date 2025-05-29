package com.axelor.apps.audio.service.impl;

import com.axelor.apps.audio.db.CustomsOffice;
import com.axelor.apps.audio.db.SoundTask;
import com.axelor.apps.audio.db.repo.SoundTaskRepository;
import com.axelor.apps.audio.service.SoundExecutorService;
import com.axelor.apps.audio.service.SoundSenderService;
import com.axelor.apps.audio.service.SoundTaskService;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import org.quartz.SchedulerException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundExecutorServiceImpl implements SoundExecutorService {
    private final SoundTaskRepository soundTaskRepository;
    private final SoundTaskService soundTaskService;
    private final SoundSenderService soundSenderService;
    private static final int MAX_CONCURRENT_FFMPEG_PER_TASK = 32;

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
        Path soundFilePath = MetaFiles.getPath(soundFile);

        int numOffices = customsOffices.size();

        int poolSizeForThisTask = Math.min(numOffices, MAX_CONCURRENT_FFMPEG_PER_TASK);
        if (poolSizeForThisTask <= 0) poolSizeForThisTask = 1;

        ExecutorService taskSpecificExecutor = Executors.newFixedThreadPool(poolSizeForThisTask);

        for (CustomsOffice office : customsOffices) {
            taskSpecificExecutor.submit(() -> {
                try {
                    soundSenderService.send(soundFilePath.toFile(), office);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        taskSpecificExecutor.shutdown();
    }

}
