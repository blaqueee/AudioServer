package com.axelor.apps.audio.service.impl;

import com.axelor.apps.audio.db.SoundTask;
import com.axelor.apps.audio.db.repo.SoundTaskRepository;
import com.axelor.apps.audio.service.SoundTaskService;
import com.axelor.apps.audio.utils.CronExpressionGenerator;
import com.axelor.meta.db.MetaSchedule;
import com.axelor.meta.db.MetaScheduleParam;
import com.axelor.meta.db.repo.MetaScheduleRepository;
import com.axelor.quartz.JobRunner;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.quartz.SchedulerException;

import java.time.LocalDateTime;

public class SoundTaskServiceImpl implements SoundTaskService {
    private final SoundTaskRepository soundTaskRepository;
    private final MetaScheduleRepository metaScheduleRepository;
    private final JobRunner jobRunner;
    private final String JOB_CLASS = "com.axelor.apps.audio.job.SoundExecutorJob";

    @Inject
    public SoundTaskServiceImpl(SoundTaskRepository soundTaskRepository, MetaScheduleRepository metaScheduleRepository, JobRunner jobRunner) {
        this.soundTaskRepository = soundTaskRepository;
        this.metaScheduleRepository = metaScheduleRepository;
        this.jobRunner = jobRunner;
    }

    @Override
    @Transactional(rollbackOn = {Exception.class})
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

    @Override
    @Transactional(rollbackOn = {Exception.class})
    public SoundTask update(SoundTask soundTask) {
        soundTask = soundTaskRepository.find(soundTask.getId());

        MetaSchedule schedule = createOrUpdateSchedule(soundTask);
        soundTask.setMetaSchedule(schedule);
        return soundTaskRepository.save(soundTask);
    }

    private MetaSchedule createOrUpdateSchedule(SoundTask soundTask) {
        MetaSchedule schedule = soundTask.getMetaSchedule();

        if (schedule == null) schedule = new MetaSchedule();

        schedule.setActive(soundTask.getIsActive());
        schedule.setName(soundTask.getName());
        schedule.setJob(JOB_CLASS);

        String cronExpression = soundTask.getOneTimeTask() ? CronExpressionGenerator.generateOneTimeCronExpression(LocalDateTime.of(
                soundTask.getExecutionDate(),
                soundTask.getExecutionTime())) :
                CronExpressionGenerator.generatePeriodicCronExpression(soundTask.getExecutionTime(),
                        soundTask.getDaysOfWeek());
        schedule.setCron(cronExpression);

        MetaScheduleParam param = new MetaScheduleParam();
        param.setName("soundTaskId");
        param.setValue(String.valueOf(soundTask.getId()));
        schedule.addParam(param);

        return metaScheduleRepository.save(schedule);
    }
}
