package com.training.assetmanagement.notification.service;

import com.training.assetmanagement.access.service.CurrentActor;
import com.training.assetmanagement.notification.domain.Notification;
import com.training.assetmanagement.notification.domain.NotificationSortField;
import com.training.assetmanagement.notification.repository.NotificationRepository;
import com.training.assetmanagement.shared.web.BusinessException;
import com.training.assetmanagement.shared.web.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// Không ghi Audit: notifications là nhật ký, không phải dữ liệu nghiệp vụ
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    // Thứ tự cố định: Mới nhất trước, id (UUIDv7) - bỏ qua sort client gửi
    private static final Sort NEWEST_FIRST = Sort.by(
        Sort.Order.desc(NotificationSortField.CREATED_AT.getProperty()),
        Sort.Order.desc(NotificationSortField.ID.getProperty())
    );

    private final NotificationRepository notificationRepository;
    private final CurrentActor currentActor;

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> list(boolean unreadOnly, Pageable pageable) {
        UUID recipientId = currentActor.get().userId();
        Pageable page = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), NEWEST_FIRST);
        return unreadOnly
            ? notificationRepository.findByRecipientIdAndReadFalse(recipientId, page)
            : notificationRepository.findByRecipientId(recipientId, page);
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCount() {
        return notificationRepository.countByRecipientIdAndReadFalse(currentActor.get().userId());
    }

    // Idempotent: Đã đọc -> vẫn thành công; không phải của mình / không tồn tại -> 404 (không tiết lộ tồn tại)
    @Override
    @Transactional
    public void markRead(UUID id) {
        UUID recipientId = currentActor.get().userId();
        if (notificationRepository.markRead(id, recipientId) == 0
                && !notificationRepository.existsByIdAndRecipientId(id, recipientId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public void markAllRead() {
        notificationRepository.markAllRead(currentActor.get().userId());
    }
}
