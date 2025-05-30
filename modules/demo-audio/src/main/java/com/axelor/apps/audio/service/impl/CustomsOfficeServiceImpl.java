package com.axelor.apps.audio.service.impl;

import com.axelor.apps.audio.db.CustomsOffice;
import com.axelor.apps.audio.db.repo.CustomsOfficeRepo;
import com.axelor.apps.audio.service.CustomsOfficeService;
import com.google.inject.Inject;

import java.util.List;

public class CustomsOfficeServiceImpl implements CustomsOfficeService {

    private final CustomsOfficeRepo customsOfficeRepo;

    @Inject
    public CustomsOfficeServiceImpl(CustomsOfficeRepo customsOfficeRepo) {
        this.customsOfficeRepo = customsOfficeRepo;
    }

    @Override
    public List<CustomsOffice> getAllParents() {
        return customsOfficeRepo.findAllParent();
    }

    @Override
    public List<CustomsOffice> getAllByParentId(Long id) {
        return customsOfficeRepo.findAllByParentId(id);
    }
}
