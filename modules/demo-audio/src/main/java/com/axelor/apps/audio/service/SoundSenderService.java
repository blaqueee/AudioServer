package com.axelor.apps.audio.service;

import com.axelor.apps.audio.db.CustomsOffice;

public interface SoundSenderService {
    void send(Long id, CustomsOffice customsOffice) throws Exception;
}
