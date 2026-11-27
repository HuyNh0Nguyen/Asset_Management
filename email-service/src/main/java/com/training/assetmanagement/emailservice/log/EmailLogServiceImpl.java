package com.training.assetmanagement.emailservice.log;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailLogServiceImpl implements EmailLogService {

    // Giống backend: Đủ đọc nguyên nhn, không phình bảng
    private static final int LAST_ERROR_MAX_LENGTH = 2000;

    private final EmailLogRepository emailLogRepository;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public Optional<EmailLog> find(UUID id) {
        return emailLogRepository.findById(id);
    }

    @Override
    @Transactional
    public boolean markSent(UUID id) {
        return emailLogRepository.compareAndSetStatus(id, EmailLogStatus.PENDING, EmailLogStatus.SENT, now()) == 1;
    }

    @Override
    @Transactional
    public boolean markFailed(UUID id, String lastError) {
        return emailLogRepository.compareAndSetStatusWithError(id, EmailLogStatus.PENDING, EmailLogStatus.FAILED,
            truncate(lastError), now()) == 1;
    }

    @Override
    @Transactional
    public boolean scheduleRetry(UUID id, int seenAttempt, String lastError) {
        return emailLogRepository.incrementAttempt(id, EmailLogStatus.PENDING, seenAttempt,
            truncate(lastError), now()) == 1;
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private static String truncate(String value) {
        if (value == null || value.length() <= LAST_ERROR_MAX_LENGTH) {
            return value;
        }
        return value.substring(0, LAST_ERROR_MAX_LENGTH);
    }
}
