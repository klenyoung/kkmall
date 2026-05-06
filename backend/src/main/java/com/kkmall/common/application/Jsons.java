package com.kkmall.common.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class Jsons {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Jsons() {}

    public static String write(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalArgumentException("JSON_INVALID");
        }
    }

    public static List<String> readStringList(String json) {
        try {
            if (json == null) return Collections.emptyList();
            return MAPPER.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    public static Map<String, Object> readMap(String json) {
        try {
            if (json == null) return Collections.emptyMap();
            return MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }

    public static List<Map<String, Object>> readMapList(String json) {
        try {
            if (json == null) return Collections.emptyList();
            return MAPPER.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }
}
