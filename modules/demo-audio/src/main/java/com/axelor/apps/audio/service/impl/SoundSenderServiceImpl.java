package com.axelor.apps.audio.service.impl;

import com.axelor.apps.audio.db.CustomsOffice;
import com.axelor.apps.audio.service.SoundSenderService;
import com.axelor.apps.audio.utils.ConstantVars;

import java.io.File;
import java.io.IOException;

public class SoundSenderServiceImpl implements SoundSenderService {

    @Override
    public void send(File soundFile, CustomsOffice customsOffice) throws IOException {
        if (soundFile == null || customsOffice == null || customsOffice.getTcpAddress() == null) return;

        ProcessBuilder builder = new ProcessBuilder(
                ConstantVars.FFMPEG_PATH,
                "-re",
                "-i", soundFile.getAbsolutePath(),
                "-acodec", "libmp3lame",
                "-ab", "-128k",
                "-f", "mp3",
                customsOffice.getTcpAddress()
        );
        builder.redirectErrorStream(true);
        builder.inheritIO();
        builder.start();
    }

}
