package com.tricore.crmtoyota.controller;

import com.tricore.crmtoyota.dto.AuthDtos.AuthResponse;
import com.tricore.crmtoyota.dto.AuthDtos.LoginRequest;
import com.tricore.crmtoyota.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "01. Autenticación y Turno", description = "Login JWT de 14h y renovación silenciosa sin interrupción en mostrador")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuario por rol (ADMIN, RECEPCIONISTA, ALMACEN)")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar silenciosamente el token JWT durante el turno")
    public AuthResponse refresh(Authentication authentication) {
        return authService.refreshSession(authentication.getName());
    }
}
