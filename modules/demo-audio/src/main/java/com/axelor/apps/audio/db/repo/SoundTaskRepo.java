package com.axelor.apps.audio.db.repo;

import com.axelor.apps.audio.db.SoundTask;

public class SoundTaskRepo extends SoundTaskRepository {

    @Override
    public SoundTask copy(SoundTask entity, boolean deep) {
        SoundTask copy = super.copy(entity, deep);
        copy.setMetaSchedule(null);
        return copy;
    }

}
