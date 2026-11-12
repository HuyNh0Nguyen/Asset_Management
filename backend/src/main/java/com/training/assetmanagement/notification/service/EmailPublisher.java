package com.training.assetmanagement.notification.service;

import com.training.assetmanagement.notification.domain.EmailMessage;

public interface EmailPublisher {

    // Gọi TRONG giao dịch nghiệp vụ; message chỉ lên RabbitMQ sau khi commit
    void publishAfterCommit(EmailMessage message);
}
