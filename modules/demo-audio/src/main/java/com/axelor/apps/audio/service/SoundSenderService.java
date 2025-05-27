package com.axelor.apps.audio.service;

import com.axelor.apps.audio.db.CustomsOffice;

import java.io.File;
import java.io.IOException;

public interface SoundSenderService {
    void send(File soundFile, CustomsOffice customsOffice) throws IOException;
}
