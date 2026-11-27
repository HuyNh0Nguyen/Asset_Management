package com.training.assetmanagement.emailservice.delivery;

import com.training.assetmanagement.emailservice.messaging.EmailMessage;

public interface EmailDeliveryService {

    // deferrals: Số lần đã hoãn liên tiếp vì CSDL không tới được (header x-email-deferral)
    void deliver(EmailMessage message, int deferrals);
}
