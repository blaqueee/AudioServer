package com.axelor.apps.audio.service;

import com.axelor.apps.audio.db.SoundTask;
import org.quartz.SchedulerException;

public interface SoundTaskService {
    SoundTask disable(SoundTask soundTask) throws SchedulerException;
    SoundTask update(Long id) throws SchedulerException;
}
