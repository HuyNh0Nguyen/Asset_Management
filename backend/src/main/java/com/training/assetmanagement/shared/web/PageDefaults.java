package com.training.assetmanagement.shared.web;

// Dùng trong @PageableDefault(size = PageDefaults.DEFAULT_SIZE); trần 100 đặt ở spring.data.web.pageable
public final class PageDefaults {

    public static final int DEFAULT_SIZE = 20;

    private PageDefaults() {}
}
