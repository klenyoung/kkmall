package com.kkmall.catalog.application;

import com.kkmall.common.exception.BusinessException;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageUploadApplicationService {
    private static final Logger log = LoggerFactory.getLogger(ImageUploadApplicationService.class);
    private final MinioClient minioClient;
    private final String bucket;
    private final String publicBaseUrl;
    private final Path localRoot;

    public ImageUploadApplicationService(@Value("${kkmall.storage.minio.endpoint}") String endpoint,
                                         @Value("${kkmall.storage.minio.access-key}") String accessKey,
                                         @Value("${kkmall.storage.minio.secret-key}") String secretKey,
                                         @Value("${kkmall.storage.minio.bucket}") String bucket,
                                         @Value("${kkmall.storage.minio.public-base-url}") String publicBaseUrl,
                                         @Value("${kkmall.storage.local-dir}") String localDir) {
        this.minioClient = MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
        this.bucket = bucket;
        this.publicBaseUrl = publicBaseUrl.replaceAll("/+$", "");
        this.localRoot = Paths.get(localDir).toAbsolutePath().normalize();
    }

    public Map<String, Object> uploadImage(MultipartFile file) {
        return uploadImage(file, "products");
    }

    public Map<String, Object> uploadImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) throw new BusinessException("UPLOAD_FILE_EMPTY");
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!contentType.startsWith("image/")) throw new BusinessException("UPLOAD_IMAGE_ONLY");
        try {
            ensureBucket();
            String objectName = objectName(file.getOriginalFilename(), folder);
            try (java.io.InputStream input = file.getInputStream()) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .contentType(contentType)
                        .stream(input, file.getSize(), -1)
                        .build());
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("url", "/api/v1/files/" + objectName);
            result.put("publicUrl", publicBaseUrl + "/" + objectName);
            result.put("objectName", objectName);
            return result;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("MinIO upload failed, fallback to local storage: {}", ex.getMessage(), ex);
            return saveLocal(file, contentType, folder);
        }
    }

    public InputStream getObject(String objectName) {
        try {
            if (objectName.startsWith("local/")) return Files.newInputStream(resolveLocal(objectName));
            return minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(objectName).build());
        } catch (Exception ex) {
            try {
                return Files.newInputStream(resolveObject(objectName));
            } catch (Exception ignored) {
                // Fall through to the public file fallback.
            }
            throw new BusinessException("FILE_NOT_FOUND");
        }
    }

    private Map<String, Object> saveLocal(MultipartFile file, String contentType, String folder) {
        try {
            String objectName = "local/" + objectName(file.getOriginalFilename(), folder);
            Path target = resolveLocal(objectName);
            Files.createDirectories(target.getParent());
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("url", "/api/v1/files/" + objectName);
            result.put("publicUrl", "/api/v1/files/" + objectName);
            result.put("objectName", objectName);
            result.put("storage", "local");
            result.put("contentType", contentType);
            return result;
        } catch (Exception localEx) {
            log.error("Local upload fallback failed: {}", localEx.getMessage(), localEx);
            throw new BusinessException("UPLOAD_FAILED");
        }
    }

    private Path resolveLocal(String objectName) {
        Path target = localRoot.resolve(objectName).normalize();
        if (!target.startsWith(localRoot)) throw new BusinessException("FILE_NOT_FOUND");
        return target;
    }

    private Path resolveObject(String objectName) {
        Path target = localRoot.resolve(objectName).normalize();
        if (!target.startsWith(localRoot)) throw new BusinessException("FILE_NOT_FOUND");
        return target;
    }

    private void ensureBucket() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            String policy = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::" + bucket + "/*\"]}]}";
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucket).config(policy).build());
        }
    }

    private String objectName(String originalName, String folder) {
        String ext = ".jpg";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase(Locale.ROOT);
        }
        String safeFolder = "avatars".equals(folder) ? "avatars" : "products";
        return safeFolder + "/" + LocalDate.now() + "/" + UUID.randomUUID() + ext;
    }
}
