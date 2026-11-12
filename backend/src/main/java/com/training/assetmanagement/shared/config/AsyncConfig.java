package com.training.assetmanagement.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

// Bật @Async; pool cấu hình ở spring.task.execution.pool
@Configuration
@EnableAsync
public class AsyncConfig {
}
