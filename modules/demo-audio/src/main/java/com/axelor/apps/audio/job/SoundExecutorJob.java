package com.axelor.apps.audio.job;

import com.axelor.apps.audio.service.SoundExecutorService;
import com.google.inject.Inject;
import org.quartz.Job;
import org.quartz.JobDataMap;
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
        JobDataMap map = jobExecutionContext.getJobDetail().getJobDataMap();
        if (map == null) return;
        Long soundTaskId = map.getLongFromString("soundTaskId");
        if (soundTaskId == null) return;

        soundExecutorService.play(soundTaskId);
    }

}