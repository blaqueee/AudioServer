package com.axelor.apps.audio.helper;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.StringReader;
import java.util.Map;

public class JsonConvert {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert to JSON", e);
        }
    }

    public static Map<String, Object> fromJson(String message) throws ScriptException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        String script = "Java.asJSONCompatible(" + message + ")";
        Object result = engine.eval(script);
        return (Map<String, Object>) result;
    }
}
