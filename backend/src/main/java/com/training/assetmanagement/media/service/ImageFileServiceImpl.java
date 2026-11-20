package com.training.assetmanagement.media.service;

import com.training.assetmanagement.access.service.CurrentActor;
import com.training.assetmanagement.media.domain.ImageFile;
import com.training.assetmanagement.media.domain.ImageVariant;
import com.training.assetmanagement.media.repository.ImageFileRepository;
import com.training.assetmanagement.shared.persistence.UuidV7;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageFileServiceImpl implements ImageFileService {

    private final ImageFileRepository imageFileRepository;
    private final ObjectStorage objectStorage;
    private final PresignedUrlSigner presignedUrlSigner;
    private final CurrentActor currentActor;
    private final Clock clock;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public UUID register(String objectKey) {
        ImageFile file = ImageFile.create(UuidV7.generate(clock), objectKey, currentActor.get().userId(),
            LocalDateTime.now(clock));
        imageFileRepository.save(file);
        return file.getId();
    }

    // Gỡ: Xóa dòng trước, tệp sau commit - lỗi giữa chừng chỉ để lại tệp mồ côi vô hại
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void delete(UUID imageFileId) {
        imageFileRepository.findById(imageFileId).ifPresent(file -> {
            imageFileRepository.delete(file);
            String objectKey = file.getObjectKey();
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    for (ImageVariant variant : ImageVariant.values()) {
                        objectStorage.removeQuietly(variant.keyOf(objectKey));
                    }
                }
            });
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, String> thumbnailUrls(Collection<UUID> imageFileIds) {
        if (imageFileIds.isEmpty()) {
            return Map.of();
        }
        return imageFileRepository.findAllById(Set.copyOf(imageFileIds)).stream()
            .collect(Collectors.toMap(ImageFile::getId, file -> presignedUrlSigner.thumbnailUrl(file.getObjectKey())));
    }
}
