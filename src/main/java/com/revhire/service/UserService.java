package com.revhire.service;

import com.revhire.dto.EmployerRegistrationDto;
import com.revhire.dto.JobSeekerRegistrationDto;
import com.revhire.exception.ResourceNotFoundException;
import com.revhire.model.User;
import com.revhire.model.JobSeeker;
import com.revhire.model.Employer;
import com.revhire.repository.JobSeekerRepository;
import com.revhire.repository.EmployerRepository;
import com.revhire.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final EmployerRepository employerRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
            JobSeekerRepository jobSeekerRepository,
            EmployerRepository employerRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jobSeekerRepository = jobSeekerRepository;
        this.employerRepository = employerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ========================= JOB SEEKER REGISTRATION =========================

    @Transactional
    public JobSeeker registerJobSeeker(JobSeekerRegistrationDto dto) {

        String email = dto.getEmail().trim().toLowerCase();

        log.info("Attempting to register JobSeeker with email: {}", email);

        // Business Validation: Email uniqueness
        if (userRepository.existsByEmail(email)) {
            log.warn("Registration failed - Email already exists: {}", email);
            throw new IllegalArgumentException("Email is already registered.");
        }

        // Double safety password check (even though controller checks)
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        try {
            JobSeeker jobSeeker = new JobSeeker();

            jobSeeker.setFullName(dto.getFullName().trim());
            jobSeeker.setEmail(email);
            jobSeeker.setPassword(passwordEncoder.encode(dto.getPassword()));
            jobSeeker.setRole(User.Role.JOBSEEKER);
            jobSeeker.setPhoneNumber(dto.getPhoneNumber().trim());
            jobSeeker.setLocation(dto.getLocation().trim());
            jobSeeker.setCurrentEmploymentStatus(dto.getCurrentEmploymentStatus());

            jobSeeker.setSecurityQuestion(dto.getSecurityQuestion());
            // In a real application, you would hash the answer or standardize its format
            jobSeeker.setSecurityAnswer(dto.getSecurityAnswer().trim().toLowerCase());

            jobSeeker.setCurrentCompany(
                    dto.getCurrentCompany() != null ? dto.getCurrentCompany().trim() : null);

            jobSeeker.setDesignation(
                    dto.getDesignation() != null ? dto.getDesignation().trim() : null);

            jobSeeker.setTotalExperienceYears(dto.getTotalExperienceYears());
            jobSeeker.setActive(true);
            jobSeeker.setCreatedAt(LocalDateTime.now());

            JobSeeker saved = jobSeekerRepository.save(jobSeeker);

            log.info("JobSeeker registered successfully with ID: {}", saved.getUserId());
            return saved;

        } catch (Exception e) {
            log.error("Error occurred while registering JobSeeker: ", e);
            throw new RuntimeException("Registration failed. Please try again.");
        }
    }

    // ========================= EMPLOYER REGISTRATION =========================

    @Transactional
    public Employer registerEmployer(EmployerRegistrationDto dto) {

        String email = dto.getEmail().trim().toLowerCase();

        log.info("Attempting to register Employer with email: {}", email);

        // Email uniqueness
        if (userRepository.existsByEmail(email)) {
            log.warn("Registration failed - Email already exists: {}", email);
            throw new IllegalArgumentException("Email is already registered.");
        }

        // Password match validation
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        try {
            Employer employer = new Employer();

            employer.setFullName(dto.getFullName().trim());
            employer.setEmail(email);
            employer.setPassword(passwordEncoder.encode(dto.getPassword()));
            employer.setRole(User.Role.EMPLOYER);

            employer.setSecurityQuestion(dto.getSecurityQuestion());
            employer.setSecurityAnswer(dto.getSecurityAnswer().trim().toLowerCase());

            employer.setCompanyName(dto.getCompanyName().trim());
            employer.setIndustry(dto.getIndustry().trim());
            employer.setCompanySize(dto.getCompanySize());
            employer.setCompanyWebsite(dto.getCompanyWebsite().trim());
            employer.setCompanyDescription(dto.getCompanyDescription().trim());
            employer.setHeadquarters(dto.getHeadquarters().trim());

            employer.setActive(true);
            employer.setCreatedAt(LocalDateTime.now());

            Employer saved = employerRepository.save(employer);

            log.info("Employer registered successfully with ID: {}", saved.getUserId());
            return saved;

        } catch (Exception e) {
            log.error("Error occurred while registering Employer: ", e);
            throw new RuntimeException("Registration failed. Please try again.");
        }
    }

    // ========================= FETCH METHODS =========================

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
}