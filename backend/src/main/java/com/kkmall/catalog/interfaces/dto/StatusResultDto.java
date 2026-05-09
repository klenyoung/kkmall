package com.kkmall.catalog.interfaces.dto;

import lombok.Data;

/**
 * 状态变更结果 DTO。
 */
@Data
public class StatusResultDto {

    private Long id;
    private String status;

    public static StatusResultDto of(Long id, String status) {
        StatusResultDto dto = new StatusResultDto();
        dto.setId(id);
        dto.setStatus(status);
        return dto;
    }
}
