package com.training.assetmanagement.notification.service;

import com.training.assetmanagement.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

// Phía đọc của chuông - người nhận luôn là người đang đăng nhập
public interface NotificationService {

    Page<Notification> list(boolean unreadOnly, Pageable pageable);

    long unreadCount();

    void markRead(UUID id);

    void markAllRead();
}
