package com.revhire.config;

import com.revhire.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final CustomUserDetailsService customUserDetailsService;
        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                        JwtAuthenticationFilter jwtAuthenticationFilter) {
                this.customUserDetailsService = customUserDetailsService;
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        }

        // PASSWORD ENCODER
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        // AUTH PROVIDER
        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(customUserDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder());
                return authProvider;
        }

        // SECURITY CONTEXT (SESSION BASED)
        @Bean
        public SecurityContextRepository securityContextRepository() {
                return new HttpSessionSecurityContextRepository();
        }

        // FILTER CHAIN ⭐⭐⭐
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

                http

                                // CSRF CONFIG
                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers(

                                                                "/api/**",

                                                                // ✅ AJAX / FETCH ENDPOINTS ⭐⭐⭐
                                                                "/jobseeker/applyJob/**",
                                                                "/jobseeker/saveJob/**",
                                                                "/jobseeker/removeSaved/**"

                                                ))

                                // SESSION MANAGEMENT
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                                                .maximumSessions(1)
                                                .maxSessionsPreventsLogin(false))

                                // SECURITY CONTEXT
                                .securityContext(context -> context
                                                .securityContextRepository(securityContextRepository()))

                                // AUTHORIZATION RULES ⭐⭐⭐

                                .authorizeHttpRequests(authz -> authz

                                                // PUBLIC ROUTES
                                                .requestMatchers(
                                                                "/",
                                                                "/index",
                                                                "/auth/login",
                                                                "/auth/forgot-password",
                                                                "/auth/reset-password",
                                                                "/auth/register/**",
                                                                "/css/**",
                                                                "/js/**",
                                                                "/images/**",
                                                                "/employer/public/**",
                                                                "/jobs/all",
                                                                "/jobs/search-page",
                                                                "/jobs/view/**",
                                                                "/favicon.ico")
                                                .permitAll()

                                                // ROLE-BASED ACCESS
                                                .requestMatchers("/jobseeker/**").hasRole("JOBSEEKER")
                                                .requestMatchers("/employer/**").hasRole("EMPLOYER")

                                                // EVERYTHING ELSE REQUIRES LOGIN
                                                .anyRequest().authenticated())

                                // AUTH PROVIDER
                                .authenticationProvider(authenticationProvider())

                                // JWT FILTER (SAFE TO KEEP)
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                                // FORM LOGIN ⭐⭐⭐
                                .formLogin(form -> form
                                                .loginPage("/auth/login")
                                                .loginProcessingUrl("/auth/login")
                                                .usernameParameter("email")
                                                .passwordParameter("password")

                                                // SUCCESS HANDLER
                                                .successHandler((request, response, authentication) -> {

                                                        String role = authentication.getAuthorities()
                                                                        .stream()
                                                                        .map(GrantedAuthority::getAuthority)
                                                                        .findFirst()
                                                                        .orElse("");

                                                        if (role.equals("ROLE_EMPLOYER")) {
                                                                response.sendRedirect("/employer/dashboard");
                                                                return;
                                                        }

                                                        response.sendRedirect("/jobseeker/dashboard");
                                                })

                                                .failureUrl("/auth/login?error")
                                                .permitAll())

                                // LOGOUT
                                .logout(logout -> logout
                                                .logoutRequestMatcher(new AntPathRequestMatcher("/auth/logout"))
                                                .logoutSuccessUrl("/auth/login?logout")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll());

                return http.build();
        }
}