package com.nexus.backend.user;

import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.security.TenantContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserResponse> findAll() {
        return userRepository.findAllByTenantId(TenantContext.get()).stream()
                .map(UserResponse::from)
                .toList();
    }
}
