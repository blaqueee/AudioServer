package com.axelor.apps.audio.service;

import com.axelor.apps.audio.dto.CustomsOfficeResponseDto;

public interface CustomsOfficeService {

    CustomsOfficeResponseDto getAllParents();
    CustomsOfficeResponseDto getAllByParentId(Long id);
}
