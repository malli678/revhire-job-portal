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

/**
 * ApiAuthController handles authentication related API endpoints.
 *
 * Responsibilities:
 * - Authenticate users using email and password.
 * - Generate JWT token for authenticated users.
 * - Validate JWT tokens sent from client requests.
 *
 * These APIs are mainly used for REST clients such as
 * mobile applications or frontend frameworks.
 */
@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

    /**
     * AuthenticationManager is used to authenticate user credentials.
     */
    private final AuthenticationManager authenticationManager;

    /**
     * JwtUtil is used to generate and validate JWT tokens.
     */
    private final JwtUtil jwtUtil;

    /**
     * UserRepository is used to fetch user details from the database.
     */
    private final UserRepository userRepository;

    /**
     * Constructor for dependency injection.
     *
     * @param authenticationManager handles authentication logic
     * @param jwtUtil utility class for JWT operations
     * @param userRepository repository used to access user data
     */
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

    /**
     * Authenticates a user using email and password.
     *
     * Steps performed:
     * - Receives login credentials in JSON format.
     * - Authenticates the user using Spring Security AuthenticationManager.
     * - Fetches user details from the database.
     * - Extracts user role.
     * - Generates a JWT token.
     * - Returns authentication details and token in the response.
     *
     * @param credentials map containing email and password
     * @return JWT token and user details if authentication succeeds
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {

        try {
            String email = credentials.get("email");
            String password = credentials.get("password");

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            //  Fetch user safely 
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            //  Extract role from Spring Security 
            String role = authentication.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse(user.getRole().name());

            //  Generate JWT 
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

    /**
     * Validates a JWT token sent in the Authorization header.
     *
     * Steps performed:
     * - Extracts token from the Authorization header.
     * - Removes the "Bearer " prefix if present.
     * - Validates the token using JwtUtil.
     * - Extracts the user email from the token.
     * - Fetches user details from the database.
     * - Returns user information if the token is valid.
     *
     * @param token JWT token sent in Authorization header
     * @return validation result and user details
     */
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