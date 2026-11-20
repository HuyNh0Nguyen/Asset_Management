package com.training.assetmanagement.media.repository;

import com.training.assetmanagement.media.domain.ImageFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ImageFileRepository extends JpaRepository<ImageFile, UUID> {
}
