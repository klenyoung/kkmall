package com.kkmall.common.application;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Map 构建工具类，用于快速构建 key-value 响应。
 *
 * @author kkmall
 */
public final class Maps {

    private Maps() {
    }

    /**
     * 快速构建 LinkedHashMap，参数为交替的 key-value 对。
     *
     * @param entries key1, value1, key2, value2, ...
     * @return 有序 Map
     */
    public static Map<String, Object> of(Object... entries) {
        Map<String, Object> map = new LinkedHashMap<>(Math.max((entries.length / 2) * 4 / 3 + 1, 16));
        for (int i = 0; i < entries.length; i += 2) {
            map.put(String.valueOf(entries[i]), entries[i + 1]);
        }
        return map;
    }
}
