package com.kkmall.order.interfaces.dto;

import lombok.Data;

/**
 * 通用 ID 结果 DTO，用于创建/更新/删除操作的返回。
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
