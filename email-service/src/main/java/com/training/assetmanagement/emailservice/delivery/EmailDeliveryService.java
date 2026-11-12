package com.training.assetmanagement.emailservice.delivery;

import com.training.assetmanagement.emailservice.messaging.EmailMessage;

public interface EmailDeliveryService {

    // attempt = số lần thử lại đã hẹn trước đó (0 ở lần giao đầu tiên)
    void deliver(EmailMessage message, int attempt);
}
