package com.revhire.config;

import com.revhire.service.CustomUserDetailsService;
import com.revhire.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthenticationFilter is a custom Spring Security filter that runs once per request.
 *
 * Purpose of this filter:
 * - Extract the JWT token from the Authorization header.
 * - Extract the username from the JWT token.
 * - Validate the token using JwtUtil.
 * - Load user details using CustomUserDetailsService.
 * - If the token is valid, set authentication in the Spring Security context.
 *
 * This allows secured endpoints to be accessed only by authenticated users.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Utility class used for extracting and validating JWT tokens.
     */
    private final JwtUtil jwtUtil;

    /**
     * Service used to load user details from the database.
     */
    private final CustomUserDetailsService userDetailsService;

    /**
     * Constructor used for dependency injection.
     *
     * @param jwtUtil utility class for handling JWT operations
     * @param userDetailsService service used to load user details
     */
    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * This method runs for every incoming HTTP request.
     *
     * Steps performed:
     * - Read the Authorization header from the request.
     * - Check if the header contains a Bearer token.
     * - Extract the JWT token.
     * - Extract the username from the token.
     * - Load user details from the database.
     * - Validate the token.
     * - If valid, set authentication in SecurityContextHolder.
     * - Continue the filter chain.
     *
     * @param request the incoming HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain to pass the request further
     * @throws ServletException if servlet error occurs
     * @throws IOException if input/output error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                logger.error("Error extracting username from JWT: " + e.getMessage());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}