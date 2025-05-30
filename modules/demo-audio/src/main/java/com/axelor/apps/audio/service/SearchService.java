package com.axelor.apps.audio.service;

import java.util.List;
import java.util.Map;

public interface SearchService {
    List<Map<String, Object>> findAndSort(String model, String where, String orderBy, String name) throws ClassNotFoundException;
}
