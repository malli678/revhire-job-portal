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

    @Transactional
    public JobSeeker registerJobSeeker(JobSeekerRegistrationDto dto) {
        log.info("Registering JobSeeker: {}", dto.getEmail());

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        JobSeeker jobSeeker = new JobSeeker();
        jobSeeker.setFullName(dto.getFullName());
        jobSeeker.setEmail(dto.getEmail());
        jobSeeker.setPassword(passwordEncoder.encode(dto.getPassword()));
        jobSeeker.setRole(User.Role.JOBSEEKER);
        jobSeeker.setPhoneNumber(dto.getPhoneNumber());
        jobSeeker.setLocation(dto.getLocation());
        jobSeeker.setCurrentEmploymentStatus(dto.getCurrentEmploymentStatus());
        jobSeeker.setCurrentCompany(dto.getCurrentCompany());
        jobSeeker.setDesignation(dto.getDesignation());
        jobSeeker.setTotalExperienceYears(dto.getTotalExperienceYears());
        jobSeeker.setActive(true);
        jobSeeker.setCreatedAt(LocalDateTime.now());

        log.info("JobSeeker registered successfully: {}", dto.getEmail());
        return jobSeekerRepository.save(jobSeeker);
    }

    @Transactional
    public Employer registerEmployer(EmployerRegistrationDto dto) {
        log.info("Registering Employer: {}", dto.getEmail());

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        Employer employer = new Employer();
        employer.setFullName(dto.getFullName());
        employer.setEmail(dto.getEmail());
        employer.setPassword(passwordEncoder.encode(dto.getPassword()));
        employer.setRole(User.Role.EMPLOYER);
        employer.setCompanyName(dto.getCompanyName());
        employer.setIndustry(dto.getIndustry());
        employer.setCompanySize(dto.getCompanySize());
        employer.setCompanyWebsite(dto.getCompanyWebsite());
        employer.setCompanyDescription(dto.getCompanyDescription());
        employer.setHeadquarters(dto.getHeadquarters());
        employer.setActive(true);
        employer.setCreatedAt(LocalDateTime.now());

        log.info("Employer registered successfully: {}", dto.getEmail());
        return employerRepository.save(employer);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
}