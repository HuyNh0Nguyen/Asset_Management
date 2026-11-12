package com.training.assetmanagement.shared.web;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

// Mọi danh sách tra cứu trả về dạng trang; page đếm từ 0 (khớp MatPaginator)
public record PageResponse<T>(
        @Schema(requiredMode = REQUIRED) List<T> items,
        @Schema(requiredMode = REQUIRED) int page,
        @Schema(requiredMode = REQUIRED) int size,
        @Schema(requiredMode = REQUIRED) long totalElements,
        @Schema(requiredMode = REQUIRED) int totalPages
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
            page.getTotalElements(), page.getTotalPages());
    }

    public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
        return from(page.map(mapper));
    }
}
