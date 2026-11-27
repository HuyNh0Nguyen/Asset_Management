package com.training.assetmanagement.notification.domain;

import java.util.Optional;

// Gọi sai kênh là lỗi lập trình -> IllegalArgumentException, không có ErrorCode
public final class NotificationChannelRules {

    public static void requireInAppOnly(NotificationEventType eventType) {
        if (eventType.emailCounterpart().isPresent()) {
            throw new IllegalArgumentException(
                "Event " + eventType + " also has an email channel - use publish(inApp, email)"
            );
        }
    }

    public static void requireEmailOnly(EmailEventType eventType) {
        if (NotificationEventType.inAppCounterpartOf(eventType).isPresent()) {
            throw new IllegalArgumentException(
                "Event " + eventType + " also has an in-app channel - use publish(inApp, email)"
            );
        }
    }

    public static void requireBothChannels(InAppNotification inApp, EmailNotification email) {
        Optional<EmailEventType> expected = inApp.eventType().emailCounterpart();
        if (expected.isEmpty()) {
            throw new IllegalArgumentException(
                "Event " + inApp.eventType() + " has no email channel - use publishInAppOnly"
            );
        }
        if (expected.get() != email.eventType()) {
            throw new IllegalArgumentException(
                "In-app event " + inApp.eventType() + " does not match email event " + email.eventType()
            );
        }
        if (!inApp.recipientId().equals(email.recipientId())) {
            throw new IllegalArgumentException("In-app and email parts must target the same recipient");
        }
    }

    private NotificationChannelRules() {}
}
