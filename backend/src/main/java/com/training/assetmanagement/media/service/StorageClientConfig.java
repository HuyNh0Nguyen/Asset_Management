package com.training.assetmanagement.media.service;

import com.training.assetmanagement.shared.config.AppProperties;
import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Hai client MinIO: ghi qua mạng nội bộ, ký URL bằng origin của trình duyệt
@Configuration
public class StorageClientConfig {

    public static final String INTERNAL_CLIENT = "internalMinioClient";
    public static final String SIGNING_CLIENT = "signingMinioClient";

    @Bean(INTERNAL_CLIENT)
    public MinioClient internalMinioClient(AppProperties properties) {
        AppProperties.Storage storage = properties.storage();
        return MinioClient.builder()
                .endpoint(storage.internalEndpoint())
                .credentials(storage.accessKey(), storage.secretKey())
                .region(storage.signingRegion())
                .build();
    }

    @Bean(SIGNING_CLIENT)
    public MinioClient signingMinioClient(AppProperties properties) {
        AppProperties.Storage storage = properties.storage();
        // Khai vùng cố định -> ký URL là phép tính cục bộ, không gọi mạng tới public-endpoint
        return MinioClient.builder()
                .endpoint(storage.publicEndpoint())
                .credentials(storage.accessKey(), storage.secretKey())
                .region(storage.signingRegion())
                .build();
    }
}
