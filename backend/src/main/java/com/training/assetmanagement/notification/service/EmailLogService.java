package com.training.assetmanagement.notification.service;

import java.util.UUID;

public interface EmailLogService {

    // PENDING -> FAILED khi không đẩy được message lên broker
    void markFailed(UUID emailLogId, String lastError);
}
