package com.training.assetmanagement.notification.web.mapper;

import com.training.assetmanagement.notification.domain.Notification;
import com.training.assetmanagement.notification.web.dto.response.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
@RequiredArgsConstructor
public class NotificationResponseMapper {

    private final Clock clock;

    // LocalDateTime trong CSDL là giờ Asia/Ho_Chi_Minh -> trả kèm offset +07:00
    public NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
            notification.getId(),
            notification.getEventType(),
            notification.getContent(),
            notification.getLink(),
            notification.isRead(),
            notification.getCreatedAt().atZone(clock.getZone()).toOffsetDateTime()
        );
    }
}
