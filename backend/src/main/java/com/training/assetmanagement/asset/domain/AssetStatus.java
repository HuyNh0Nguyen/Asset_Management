package com.training.assetmanagement.asset.domain;

// Trạng thái tài sản cá biệt - khớp ck_assets_status
public enum AssetStatus {
    AVAILABLE,
    ALLOCATED,
    IN_MAINTENANCE,
    RECALLED,
    DISPOSED
}
