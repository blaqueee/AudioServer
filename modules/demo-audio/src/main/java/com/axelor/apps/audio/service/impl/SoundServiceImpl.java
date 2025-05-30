package com.axelor.apps.audio.service.impl;

import com.axelor.apps.audio.service.SoundService;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.google.inject.Inject;

import java.io.File;
import java.nio.file.Path;

public class SoundServiceImpl implements SoundService {

    private final MetaFileRepository metaFileRepository;

    @Inject
    public SoundServiceImpl(MetaFileRepository metaFileRepository) {
        this.metaFileRepository = metaFileRepository;
    }

    @Override
    public File getSoundFile(String id) {
        MetaFile metaFile = metaFileRepository.find(Long.valueOf(id));
        Path path = MetaFiles.getPath(metaFile);
        return path.toFile();
    }
}
