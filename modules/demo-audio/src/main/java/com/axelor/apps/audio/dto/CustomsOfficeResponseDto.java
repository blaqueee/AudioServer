package com.axelor.apps.audio.dto;

import com.axelor.apps.audio.db.CustomsOffice;

import java.util.List;

public class CustomsOfficeResponseDto {
    List<CustomsOffice> data;

    public List<CustomsOffice> getData() {
        return data;
    }

    public void setCustomsOffices(List<CustomsOffice> data) {
        this.data = data;
    }
}
