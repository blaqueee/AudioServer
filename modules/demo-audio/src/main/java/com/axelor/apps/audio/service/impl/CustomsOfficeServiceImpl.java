package com.axelor.apps.audio.service.impl;

import com.axelor.apps.audio.db.CustomsOffice;
import com.axelor.apps.audio.db.repo.CustomsOfficeRepo;
import com.axelor.apps.audio.dto.CustomsOfficeResponseDto;
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
    public CustomsOfficeResponseDto getAllParents() {
        CustomsOfficeResponseDto customsOfficeResponseDto = new CustomsOfficeResponseDto();
        List<CustomsOffice> customsOffices = customsOfficeRepo.findAllParent();
        customsOfficeResponseDto.setCustomsOffices(customsOffices);
        return customsOfficeResponseDto;
    }

    @Override
    public CustomsOfficeResponseDto getAllByParentId(Long id) {
        CustomsOfficeResponseDto customsOfficeResponseDto = new CustomsOfficeResponseDto();
        List<CustomsOffice> customsOffices = customsOfficeRepo.findAllByParentId(id);
        customsOfficeResponseDto.setCustomsOffices(customsOffices);
        return customsOfficeResponseDto;
    }
}
