package com.revhire.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.revhire.dto.EmployerRegistrationDto;
import com.revhire.dto.JobSeekerRegistrationDto;
import com.revhire.model.Employer;
import com.revhire.model.JobSeeker;
import com.revhire.model.User;
import com.revhire.repository.EmployerRepository;
import com.revhire.repository.JobSeekerRepository;
import com.revhire.repository.UserRepository;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobSeekerRepository jobSeekerRepository;

    @Mock
    private EmployerRepository employerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private JobSeekerRegistrationDto seekerDto;
    private EmployerRegistrationDto employerDto;

    @Before
    public void setUp() {
        seekerDto = new JobSeekerRegistrationDto();
        seekerDto.setFullName("John Doe");
        seekerDto.setEmail("john@seeker.com");
        seekerDto.setPassword("pass123");
        seekerDto.setConfirmPassword("pass123");
        seekerDto.setPhoneNumber("1234567890");
        seekerDto.setLocation("NYC");
        seekerDto.setSecurityQuestion("What is your pet's name?");
        seekerDto.setSecurityAnswer("Fluffy");

        employerDto = new EmployerRegistrationDto();
        employerDto.setFullName("Jane Smith");
        employerDto.setEmail("jane@employer.com");
        employerDto.setPassword("pass123");
        employerDto.setConfirmPassword("pass123");
        employerDto.setCompanyName("Tech Solutions");
        employerDto.setIndustry("IT");
        employerDto.setCompanySize("11-50");
        employerDto.setCompanyWebsite("https://tech.com");
        employerDto.setCompanyDescription("Tech firm");
        employerDto.setHeadquarters("Seattle");
        employerDto.setSecurityQuestion("City of birth?");
        employerDto.setSecurityAnswer("Seattle");
    }

    @Test
    public void testRegisterJobSeeker_Success() {
        when(userRepository.existsByEmail("john@seeker.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded_pass");

        JobSeeker savedSeeker = new JobSeeker();
        savedSeeker.setUserId(1L);
        savedSeeker.setEmail("john@seeker.com");
        when(jobSeekerRepository.save(any(JobSeeker.class))).thenReturn(savedSeeker);

        JobSeeker result = userService.registerJobSeeker(seekerDto);

        assertNotNull(result);
        assertEquals(Long.valueOf(1), result.getUserId());
        verify(userRepository, times(1)).existsByEmail("john@seeker.com");
        verify(jobSeekerRepository, times(1)).save(any(JobSeeker.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterJobSeeker_EmailExists() {
        when(userRepository.existsByEmail("john@seeker.com")).thenReturn(true);

        userService.registerJobSeeker(seekerDto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterJobSeeker_PasswordMismatch() {
        seekerDto.setConfirmPassword("wrongpass");

        userService.registerJobSeeker(seekerDto);
    }

    @Test
    public void testRegisterEmployer_Success() {
        when(userRepository.existsByEmail("jane@employer.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded_pass");

        Employer savedEmployer = new Employer();
        savedEmployer.setUserId(2L);
        savedEmployer.setEmail("jane@employer.com");
        when(employerRepository.save(any(Employer.class))).thenReturn(savedEmployer);

        Employer result = userService.registerEmployer(employerDto);

        assertNotNull(result);
        assertEquals(Long.valueOf(2), result.getUserId());
        verify(userRepository, times(1)).existsByEmail("jane@employer.com");
        verify(employerRepository, times(1)).save(any(Employer.class));
    }

    @Test
    public void testGetUserById() {
        User user = new User();
        user.setUserId(5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(5L);

        assertNotNull(result);
        assertEquals(Long.valueOf(5), result.getUserId());
    }

    @Test
    public void testFindByEmail() {
        User user = new User();
        user.setEmail("test@email.com");
        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));

        User result = userService.findByEmail("test@email.com");

        assertNotNull(result);
        assertEquals("test@email.com", result.getEmail());
    }
}
