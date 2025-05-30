package com.axelor.apps.audio.service;

import com.axelor.apps.audio.dto.SearchResponseDto;

public interface SearchService {
    SearchResponseDto findAndSort(String model, String where, String orderBy, String name) throws ClassNotFoundException;
}
