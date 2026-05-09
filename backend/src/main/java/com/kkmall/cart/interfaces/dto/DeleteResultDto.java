package com.kkmall.cart.interfaces.dto;

import lombok.Data;

/**
 * 删除操作结果 DTO。
 */
@Data
public class DeleteResultDto {

    private Boolean deleted;

    public static DeleteResultDto success() {
        DeleteResultDto dto = new DeleteResultDto();
        dto.setDeleted(true);
        return dto;
    }
}
