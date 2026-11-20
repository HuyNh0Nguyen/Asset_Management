package com.training.assetmanagement.lifecycle.repository;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "AllocationHolderRow", description = "Allocation holder row")
public record AllocationHolderRow(UUID id, String code, String name, int holding) {
}
