package com.kkmall.catalog.interfaces.dto;

import lombok.Data;

/**
 * 通用 ID 结果 DTO。
 */
@Data
public class IdResultDto {

    private Long id;

    public static IdResultDto of(Long id) {
        IdResultDto dto = new IdResultDto();
        dto.setId(id);
        return dto;
    }
}
