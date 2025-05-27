package com.axelor.apps.audio.controller;

import com.axelor.apps.audio.db.SoundTask;
import com.axelor.apps.audio.service.SoundTaskService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class SoundTaskController {
    private final SoundTaskService soundTaskService;

    @Inject
    public SoundTaskController(SoundTaskService soundTaskService) {
        this.soundTaskService = soundTaskService;
    }

    public void update(ActionRequest request, ActionResponse response) {
        SoundTask soundTask = request.getContext().asType(SoundTask.class);

        try {
            SoundTask updated = soundTaskService.update(soundTask);
            response.setValues(updated);
            response.setReload(true);
        } catch (Exception e) {
            response.setError(e.getMessage());
        }
    }
}
