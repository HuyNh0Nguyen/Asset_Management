package com.training.assetmanagement.asset.repository;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

// Một tài sản / lô gắn với đơn vị quản lý; disposed = đã thanh lý (hết)
@Schema(name = "AssetUsageRow", description = "An asset or lot associated with a managing unit; disposed = already liquidated (finished)")
public record AssetUsageRow(UUID id, String code, String name, boolean disposed) {
}
