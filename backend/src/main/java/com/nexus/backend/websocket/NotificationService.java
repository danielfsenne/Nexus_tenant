package com.nexus.backend.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyTenant(Long tenantId, String type, String message) {
        messagingTemplate.convertAndSend(
                "/topic/tenant/" + tenantId + "/notifications",
                NotificationPayload.of(type, message)
        );
    }
}
