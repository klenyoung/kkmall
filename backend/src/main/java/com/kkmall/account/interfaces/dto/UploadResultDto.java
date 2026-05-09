package com.kkmall.account.interfaces.dto;

import lombok.Data;

/**
 * 文件上传结果 DTO。
 */
@Data
public class UploadResultDto {

    private String url;

    private String publicUrl;

    private String objectName;
}
