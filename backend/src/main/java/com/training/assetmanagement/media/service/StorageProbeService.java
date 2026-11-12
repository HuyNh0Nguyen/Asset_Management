package com.training.assetmanagement.media.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

// Chỉ để nghiệm thu: ảnh hiện qua URL ký ở cả local và dev
public interface StorageProbeService {

    StorageProbeResult upload(MultipartFile file);

    StorageProbeResult refreshUrl(UUID probeId);
}
