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

/**
 * SecurityConfig is the main configuration class for Spring Security.
 *
 * Responsibilities:
 * - Configure authentication provider
 * - Configure password encoding
 * - Define security rules for routes
 * - Manage session handling
 * - Add JWT authentication filter
 * - Configure login and logout functionality
 *
 * This class ensures that only authorized users can access protected resources
 * based on their roles (JOBSEEKER or EMPLOYER).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

        /**
         * CustomUserDetailsService is used to load user data from the database.
         */
        private final CustomUserDetailsService customUserDetailsService;

        /**
         * JWT authentication filter used to validate JWT tokens in requests.
         */
        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        /**
         * Constructor used for dependency injection.
         *
         * @param customUserDetailsService service used to load user details
         * @param jwtAuthenticationFilter filter used for JWT authentication
         */
        public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                        JwtAuthenticationFilter jwtAuthenticationFilter) {
                this.customUserDetailsService = customUserDetailsService;
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        }

        /**
         * Creates a password encoder bean.
         *
         * BCryptPasswordEncoder is used to securely hash user passwords
         * before storing them in the database.
         *
         * @return PasswordEncoder instance
         */
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        /**
         * Configures the authentication provider.
         *
         * DaoAuthenticationProvider uses CustomUserDetailsService to load
         * user information and PasswordEncoder to verify passwords.
         *
         * @return DaoAuthenticationProvider instance
         */
        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(customUserDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder());
                return authProvider;
        }

        /**
         * Configures security context storage.
         *
         * HttpSessionSecurityContextRepository stores authentication
         * information inside the HTTP session.
         *
         * @return SecurityContextRepository instance
         */
        @Bean
        public SecurityContextRepository securityContextRepository() {
                return new HttpSessionSecurityContextRepository();
        }

        /**
         * Main security filter chain configuration.
         *
         * This method defines:
         * - CSRF configuration
         * - Session management
         * - Authorization rules
         * - Authentication provider
         * - JWT authentication filter
         * - Form login configuration
         * - Access denied handling
         * - Logout behavior
         *
         * @param http HttpSecurity object used to configure security settings
         * @return SecurityFilterChain configuration
         * @throws Exception if configuration fails
         */
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

                http

                                // CSRF CONFIG
                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers(

                                                                "/api/**",

                                                                // AJAX / FETCH ENDPOINTS
                                                                "/jobseeker/applyJob/**",
                                                                "/jobseeker/saveJob/**",
                                                                "/jobseeker/removeSaved/**"

                                                ))

                                // SESSION MANAGEMENT
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                                                .maximumSessions(-1)
                                                .maxSessionsPreventsLogin(false))

                                // SECURITY CONTEXT
                                .securityContext(context -> context
                                                .securityContextRepository(securityContextRepository()))

                                // AUTHORIZATION RULES
                                .authorizeHttpRequests(authz -> authz

                                                // PUBLIC ROUTES
                                                .requestMatchers(
                                                                "/",
                                                                "/index",
                                                                "/auth/login",
                                                                "/auth/forgot-password",
                                                                "/auth/verify-security-answer",
                                                                "/auth/reset-password",
                                                                "/auth/register/**",
                                                                "/css/**",
                                                                "/js/**",
                                                                "/images/**",
                                                                "/employer/public/**",
                                                                "/jobs/all",
                                                                "/jobs/search-page",
                                                                "/jobs/view/**",
                                                                "/auth/access-denied",
                                                                "/favicon.ico")
                                                .permitAll()

                                                // ROLE-BASED ACCESS
                                                .requestMatchers("/jobseeker/**").hasRole("JOBSEEKER")
                                                .requestMatchers("/employer/**").hasRole("EMPLOYER")

                                                // EVERYTHING ELSE REQUIRES LOGIN
                                                .anyRequest().authenticated())

                                // AUTH PROVIDER
                                .authenticationProvider(authenticationProvider())

                                // JWT FILTER
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                                // FORM LOGIN
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

                                // EXCEPTION HANDLING
                                .exceptionHandling(exception -> exception
                                                .accessDeniedPage("/auth/access-denied"))

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