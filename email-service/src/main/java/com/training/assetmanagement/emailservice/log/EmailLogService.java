package com.training.assetmanagement.emailservice.log;

import java.util.Optional;
import java.util.UUID;

// Mỗi hàm một giao dịch ngắn - không giữ giao dịch qua lần gửi SMTP
public interface EmailLogService {

    Optional<EmailLog> find(UUID id);

    // PENDING -> SENT; false = đã chốt nơi khác
    boolean markSent(UUID id);

    // PENDING -> FAILED kèm lý do
    boolean markFailed(UUID id, String lastError);

    // attempt_count: seenAttempt -> seenAttempt + 1; false = dòng đã đổi
    boolean scheduleRetry(UUID id, int seenAttempt, String lastError);
}
