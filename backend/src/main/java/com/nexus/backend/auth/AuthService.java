package com.nexus.backend.auth;

import com.nexus.backend.domain.Plan;
import com.nexus.backend.domain.Role;
import com.nexus.backend.domain.Tenant;
import com.nexus.backend.domain.User;
import com.nexus.backend.repository.TenantRepository;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            TenantRepository tenantRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        Tenant tenant = tenantRepository.save(
                Tenant.builder()
                        .name(request.companyName())
                        .plan(Plan.FREE)
                        .build()
        );

        User admin = userRepository.save(
                User.builder()
                        .name(request.adminName())
                        .email(request.email())
                        .password(passwordEncoder.encode(request.password()))
                        .role(Role.ADMIN)
                        .tenantId(tenant.getId())
                        .build()
        );

        String token = jwtService.generateToken(admin);
        return new AuthResponse(token, tenant.getId(), admin.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Credenciais inválidas");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getTenantId(), user.getRole().name());
    }
}
