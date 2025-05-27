package com.axelor.apps.audio.job;

import com.axelor.apps.audio.service.SoundExecutorService;
import com.google.inject.Inject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SoundExecutorJob implements Job {
    private final SoundExecutorService soundExecutorService;

    @Inject
    public SoundExecutorJob(SoundExecutorService soundExecutorService) {
        this.soundExecutorService = soundExecutorService;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Object soundTaskIdStr = jobExecutionContext.get("soundTaskId");
        if (soundTaskIdStr == null) return;
        Long soundTaskId = Long.valueOf(soundTaskIdStr.toString());

        soundExecutorService.play(soundTaskId);
    }

}