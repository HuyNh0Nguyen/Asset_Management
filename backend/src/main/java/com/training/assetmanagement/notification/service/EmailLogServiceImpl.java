package com.training.assetmanagement.notification.service;

import com.training.assetmanagement.notification.domain.EmailLogStatus;
import com.training.assetmanagement.notification.repository.EmailLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailLogServiceImpl implements EmailLogService {

    // Giới hạn độ dài lỗi lưu lại - đủ đọc nguyên nhân, không phình bảng
    private static final int LAST_ERROR_MAX_LENGTH = 2000;

    private final EmailLogRepository emailLogRepository;
    private final Clock clock;

    // Giao dịch riêng: Chạy trên luồng @Async sau commit, giao dịch nghiệp vụ đã đóng
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(UUID emailLogId, String lastError) {
        emailLogRepository.compareAndSetStatus(emailLogId, EmailLogStatus.PENDING, EmailLogStatus.FAILED,
            truncate(lastError), LocalDateTime.now(clock));
    }

    private static String truncate(String value) {
        if (value == null || value.length() <= LAST_ERROR_MAX_LENGTH) {
            return value;
        }
        return value.substring(0, LAST_ERROR_MAX_LENGTH);
    }
}
