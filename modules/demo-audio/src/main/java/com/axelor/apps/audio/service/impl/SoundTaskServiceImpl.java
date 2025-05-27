package com.axelor.apps.audio.service.impl;

import com.axelor.apps.audio.db.SoundTask;
import com.axelor.apps.audio.db.repo.SoundTaskRepository;
import com.axelor.apps.audio.service.SoundTaskService;
import com.axelor.meta.db.MetaSchedule;
import com.axelor.meta.db.repo.MetaScheduleRepository;
import com.axelor.quartz.JobRunner;
import com.google.inject.Inject;
import org.quartz.SchedulerException;

public class SoundTaskServiceImpl implements SoundTaskService {
    private final SoundTaskRepository soundTaskRepository;
    private final MetaScheduleRepository metaScheduleRepository;
    private final JobRunner jobRunner;

    @Inject
    public SoundTaskServiceImpl(SoundTaskRepository soundTaskRepository, MetaScheduleRepository metaScheduleRepository, JobRunner jobRunner) {
        this.soundTaskRepository = soundTaskRepository;
        this.metaScheduleRepository = metaScheduleRepository;
        this.jobRunner = jobRunner;
    }

    @Override
    public SoundTask disable(SoundTask soundTask) throws SchedulerException {
        soundTask.setIsActive(false);
        soundTask = soundTaskRepository.save(soundTask);

        MetaSchedule schedule = soundTask.getMetaSchedule();
        if (schedule == null) return soundTask;

        schedule.setActive(false);
        schedule = metaScheduleRepository.save(schedule);
        jobRunner.update(schedule);
        return soundTask;
    }
}
