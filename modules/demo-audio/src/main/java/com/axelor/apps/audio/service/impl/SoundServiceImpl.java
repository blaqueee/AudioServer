package com.axelor.apps.audio.service.impl;

import com.axelor.apps.audio.service.SoundService;
import com.axelor.meta.MetaFiles;

import java.io.File;
import java.nio.file.Path;

public class SoundServiceImpl implements SoundService {

    @Override
    public File getSoundFile(String url) {
        Path path = MetaFiles.getPath(url);
        return path.toFile();
    }
}
