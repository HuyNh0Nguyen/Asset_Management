package com.training.assetmanagement.notification.service;

import com.training.assetmanagement.notification.domain.EmailNotification;
import com.training.assetmanagement.notification.domain.InAppNotification;

// Kênh do danh mục sự kiện quyết định; cả ba hàm bắt buộc chạy trong giao dịch nghiệp vụ
public interface NotificationPublisher {

    // Chỉ in-app
    void publishInAppOnly(InAppNotification notification);

    // Chỉ email
    void publishEmailOnly(EmailNotification notification);

    // Cả hai kênh - cùng sự kiện, cùng người nhận
    void publish(InAppNotification inApp, EmailNotification email);
}
