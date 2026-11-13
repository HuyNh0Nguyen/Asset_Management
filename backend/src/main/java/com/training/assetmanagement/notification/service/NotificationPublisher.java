package com.training.assetmanagement.notification.service;

import com.training.assetmanagement.notification.domain.EmailNotification;

public interface NotificationPublisher {

    // Kênh chỉ email: Chèn email_logs PENDING, KHÔNG chèn notifications; publish sau commit
    void publishEmailOnly(EmailNotification notification);
}
