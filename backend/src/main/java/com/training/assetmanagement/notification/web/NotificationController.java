package com.training.assetmanagement.notification.web;

import com.training.assetmanagement.notification.service.NotificationService;
import com.training.assetmanagement.notification.web.dto.response.NotificationResponse;
import com.training.assetmanagement.notification.web.dto.response.UnreadCountResponse;
import com.training.assetmanagement.notification.web.mapper.NotificationResponseMapper;
import com.training.assetmanagement.shared.web.ApiPath;
import com.training.assetmanagement.shared.web.ApiTag;
import com.training.assetmanagement.shared.web.PageDefaults;
import com.training.assetmanagement.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// Đã đăng nhập là đủ, không cần mã quyền; không có tham số recipientId
@RestController
@RequestMapping(ApiPath.NOTIFICATIONS)
@Tag(name = ApiTag.NOTIFICATIONS)
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationResponseMapper notificationResponseMapper;

    // Thứ tự cố định mới nhất trước - tham số sort bị bỏ qua
    @GetMapping
    @Operation(operationId = "listNotifications", summary = "List my notifications, newest first")
    public PageResponse<NotificationResponse> list(@RequestParam(defaultValue = "false") boolean unreadOnly,
                                                   @ParameterObject @PageableDefault(size = PageDefaults.DEFAULT_SIZE)
                                                   Pageable pageable) {
        return PageResponse.from(notificationService.list(unreadOnly, pageable), notificationResponseMapper::toResponse);
    }

    @GetMapping(ApiPath.UNREAD_COUNT)
    @Operation(operationId = "getUnreadNotificationCount", summary = "Count my unread notifications")
    public UnreadCountResponse unreadCount() {
        return new UnreadCountResponse(notificationService.unreadCount());
    }

    @PostMapping(ApiPath.MARK_READ)
    @Operation(operationId = "markNotificationRead", summary = "Mark one of my notifications as read")
    public void markRead(@PathVariable UUID id) {
        notificationService.markRead(id);
    }

    @PostMapping(ApiPath.READ_ALL)
    @Operation(operationId = "markAllNotificationsRead", summary = "Mark all my notifications as read")
    public void markAllRead() {
        notificationService.markAllRead();
    }
}
