package com.training.assetmanagement.shared.web;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// Đường dẫn giao diện dùng dựng link trong email - ghép sau app.public-base-url
@Getter
@RequiredArgsConstructor
public enum FrontendRoute {

    LOGIN("/login");

    private final String path;
}
