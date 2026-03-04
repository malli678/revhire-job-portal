package com.revhire.controller;

import com.revhire.model.User;
import com.revhire.repository.UserRepository;
import com.revhire.util.JwtUtil;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public ApiAuthController(AuthenticationManager authenticationManager,
                             JwtUtil jwtUtil,
                             UserRepository userRepository) {

        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    // =========================
    // LOGIN (JSON → JWT)
    // =========================
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {

        try {
            String email = credentials.get("email");
            String password = credentials.get("password");

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            // ✅ Fetch user safely ⭐⭐⭐
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ Extract role from Spring Security ⭐⭐⭐
            String role = authentication.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse(user.getRole().name());

            // ✅ Generate JWT ⭐⭐⭐
            String token = jwtUtil.generateToken(email, role);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", token);
            response.put("userId", user.getUserId());
            response.put("email", user.getEmail());
            response.put("fullName", user.getFullName());
            response.put("role", role);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Invalid email or password");

            return ResponseEntity.status(401).body(response);
        }
    }

    // =========================
    // TOKEN VALIDATION
    // =========================
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(
            @RequestHeader("Authorization") String token) {

        try {

            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            if (jwtUtil.validateToken(token)) {

                String email = jwtUtil.extractUsername(token);

                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                Map<String, Object> response = new HashMap<>();
                response.put("valid", true);
                response.put("userId", user.getUserId());
                response.put("email", user.getEmail());
                response.put("role", user.getRole().name());

                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            // Token invalid
        }

        return ResponseEntity.status(401).body(Map.of("valid", false));
    }
}