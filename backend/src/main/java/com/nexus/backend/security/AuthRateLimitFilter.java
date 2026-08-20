package com.nexus.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;

/**
 * Limita tentativas de login/registro por IP usando um contador no Redis
 * com janela fixa, para dificultar ataques de força bruta.
 */
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> LIMITED_PATHS = Set.of("/auth/login", "/auth/register");

    private final StringRedisTemplate redisTemplate;
    private final boolean enabled;
    private final int maxAttempts;
    private final long windowSeconds;

    public AuthRateLimitFilter(
            StringRedisTemplate redisTemplate,
            @Value("${nexus.rate-limit.enabled}") boolean enabled,
            @Value("${nexus.rate-limit.auth-max-attempts}") int maxAttempts,
            @Value("${nexus.rate-limit.auth-window-seconds}") long windowSeconds
    ) {
        this.redisTemplate = redisTemplate;
        this.enabled = enabled;
        this.maxAttempts = maxAttempts;
        this.windowSeconds = windowSeconds;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (!enabled || !LIMITED_PATHS.contains(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = "rate-limit:auth:" + request.getRemoteAddr();
        Long attempts = redisTemplate.opsForValue().increment(key);

        if (attempts != null && attempts == 1L) {
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        }

        if (attempts != null && attempts > maxAttempts) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Muitas tentativas. Tente novamente em instantes.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
