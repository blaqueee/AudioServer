package com.axelor.apps.audio.dto;

import groovy.transform.builder.Builder;

import java.util.List;
import java.util.Map;

@Builder
public class SearchResponseDto {
    private List<Map<String, Object>> data;

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }
}
