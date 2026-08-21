package com.nexus.backend.websocket;

import java.time.Instant;

public record NotificationPayload(String type, String message, Instant createdAt) {

    public static NotificationPayload of(String type, String message) {
        return new NotificationPayload(type, message, Instant.now());
    }
}
