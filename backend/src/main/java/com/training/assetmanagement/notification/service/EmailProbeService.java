package com.training.assetmanagement.notification.service;

public interface EmailProbeService {

    void sendProbe(String recipientEmail, String recipientName);
}
