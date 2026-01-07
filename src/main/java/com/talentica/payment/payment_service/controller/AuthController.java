package com.talentica.payment.payment_service.controller;

import com.talentica.payment.payment_service.security.JwtUtil;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public String login() {
        // Dummy login
        return jwtUtil.generateToken("assignment-user");
    }
}
