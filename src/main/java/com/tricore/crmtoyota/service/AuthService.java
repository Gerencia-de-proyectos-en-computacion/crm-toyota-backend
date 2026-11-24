package com.tricore.crmtoyota.service;

import com.tricore.crmtoyota.domain.entity.User;
import com.tricore.crmtoyota.domain.enums.Role;
import com.tricore.crmtoyota.dto.AuthDtos.AuthResponse;
import com.tricore.crmtoyota.dto.AuthDtos.LoginRequest;
import com.tricore.crmtoyota.repository.UserRepository;
import com.tricore.crmtoyota.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameIgnoreCase(request.username().trim())
                .filter(User::isActive)
                .orElseThrow(() -> new BadCredentialsException("Usuario o contraseña inválidos."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Usuario o contraseña inválidos.");
        }
        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshSession(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .filter(User::isActive)
                .orElseThrow(() -> new BadCredentialsException("Sesión inválida o usuario inactivo."));
        return buildAuthResponse(user);
    }

    public static int maxDiscountForRole(Role role) {
        return switch (role) {
            case ADMIN -> 15;
            case RECEPCIONISTA -> 5;
            case ALMACEN -> 0;
        };
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user);
        return new AuthResponse(
                token,
                jwtService.calculateExpiresAt(),
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getStationLabel(),
                user.getRole(),
                maxDiscountForRole(user.getRole())
        );
    }
}
