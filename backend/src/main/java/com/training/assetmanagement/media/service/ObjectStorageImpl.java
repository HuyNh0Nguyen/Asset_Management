package com.training.assetmanagement.media.service;

import com.training.assetmanagement.shared.config.AppProperties;
import com.training.assetmanagement.shared.web.BusinessException;
import com.training.assetmanagement.shared.web.ErrorCode;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class ObjectStorageImpl implements ObjectStorage {

    private static final Logger log = LoggerFactory.getLogger(ObjectStorageImpl.class);

    // -1: Để MinIO tự chọn kích thước part khi tải lên
    private static final long AUTO_PART_SIZE = -1;

    private final MinioClient internalClient;
    private final MinioClient signingClient;
    private final AppProperties.Storage storage;

    public ObjectStorageImpl(@Qualifier(StorageClientConfig.INTERNAL_CLIENT) MinioClient internalClient,
                             @Qualifier(StorageClientConfig.SIGNING_CLIENT) MinioClient signingClient,
                             AppProperties properties) {
        this.internalClient = internalClient;
        this.signingClient = signingClient;
        this.storage = properties.storage();
    }

    @Override
    public void put(String objectKey, InputStream content, long size, String contentType) {
        try {
            internalClient.putObject(PutObjectArgs.builder()
                .bucket(storage.bucket())
                .object(objectKey)
                .stream(content, size, AUTO_PART_SIZE)
                .contentType(contentType)
                .build());
        } catch (MinioException | IOException | GeneralSecurityException ex) {
            throw storageError("write", objectKey, ex);
        }
    }

    @Override
    public void remove(String objectKey) {
        try {
            internalClient.removeObject(RemoveObjectArgs.builder().bucket(storage.bucket()).object(objectKey).build());
        } catch (MinioException | IOException | GeneralSecurityException ex) {
            throw storageError("remove", objectKey, ex);
        }
    }

    @Override
    public String presignedGetUrl(String objectKey) {
        try {
            return signingClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(storage.bucket())
                .object(objectKey)
                .expiry((int) storage.presignedTtl().toSeconds(), TimeUnit.SECONDS)
                .build());
        } catch (MinioException | IOException | GeneralSecurityException ex) {
            throw storageError("presignedGetUrl", objectKey, ex);
        }
    }

    @Override
    public Duration presignedTtl() {
        return storage.presignedTtl();
    }

    private BusinessException storageError(String operation, String objectKey, Exception ex) {
        log.error("MinIO error {} for objectKey={}", operation, objectKey, ex);
        return new BusinessException(ErrorCode.STORAGE_ERROR, ex.getMessage());
    }
}
