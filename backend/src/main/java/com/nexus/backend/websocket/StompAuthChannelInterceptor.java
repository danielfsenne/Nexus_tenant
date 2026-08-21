package com.nexus.backend.websocket;

import com.nexus.backend.security.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;

/**
 * Autentica a conexão STOMP a partir do JWT enviado no header Authorization do
 * frame CONNECT, e garante que cada sessão só consiga se inscrever no tópico
 * de notificações do próprio tenant.
 */
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    public StompAuthChannelInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            authenticate(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            authorizeSubscription(accessor);
        }

        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        String header = (authHeaders == null || authHeaders.isEmpty()) ? null : authHeaders.get(0);

        if (header == null || !header.startsWith("Bearer ")) {
            throw new BadCredentialsException("Token ausente na conexão WebSocket");
        }

        Claims claims = jwtService.parseClaims(header.substring(7));
        Long tenantId = claims.get("tenantId", Long.class);
        String role = claims.get("role", String.class);
        String email = claims.getSubject();

        Principal principal = new UsernamePasswordAuthenticationToken(
                email, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        accessor.setUser(principal);

        if (accessor.getSessionAttributes() != null) {
            accessor.getSessionAttributes().put("tenantId", tenantId);
        }
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        Object tenantId = accessor.getSessionAttributes() != null ? accessor.getSessionAttributes().get("tenantId") : null;

        if (tenantId == null || destination == null || !destination.equals("/topic/tenant/" + tenantId + "/notifications")) {
            throw new AccessDeniedException("Inscrição não permitida para este tópico");
        }
    }
}
