package com.banking.controller;

import com.banking.entity.User;
import com.banking.repository.UserRepository;
import com.banking.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    record RegisterRequest(String username, String password, String email) {}
    record AuthResponse(String token, Long userId) {}

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        User user = new User(request.username(),
                passwordEncoder.encode(request.password()),
                request.email());
        userRepository.save(user);

        String token = jwtService.generateToken(request.username());
        return ResponseEntity.ok(new AuthResponse(token, user.getId()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElse(null);

        if (user != null && passwordEncoder.matches(request.password(), user.getPassword())) {
            String token = jwtService.generateToken(request.username());
            return ResponseEntity.ok(new AuthResponse(token, user.getId()));
        }

        return ResponseEntity.badRequest().body("Invalid credentials");
    }

    record LoginRequest(String username, String password) {}
}