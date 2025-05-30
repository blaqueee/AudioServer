package com.axelor.apps.audio.service;

import com.axelor.apps.audio.db.CustomsOffice;

import java.util.List;

public interface CustomsOfficeService {

    List<CustomsOffice> getAllParents();
    List<CustomsOffice> getAllByParentId(Long id);

}
