package com.axelor.apps.audio.helper;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonConvert {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert to JSON", e);
        }
    }
}
