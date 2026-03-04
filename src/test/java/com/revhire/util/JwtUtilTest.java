package com.revhire.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    private UserDetails userDetails;

    @Before
    public void setUp() {
        // Inject the @Value properties manually for pure unit testing
        ReflectionTestUtils.setField(jwtUtil, "secret", "mySuperSecretKeyThatIsAtLeast32BytesLongForHmacSha256!!!!!!");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L); // 1 hour

        userDetails = new User("testuser@example.com", "password", Collections.emptyList());
    }

    @Test
    public void testGenerateToken() {
        String token = jwtUtil.generateToken("testuser@example.com", "JOBSEEKER");

        assertNotNull(token);
        String username = jwtUtil.extractUsername(token);
        assertEquals("testuser@example.com", username);
    }

    @Test
    public void testValidateToken_Success() {
        String token = jwtUtil.generateToken("testuser@example.com", "JOBSEEKER");

        Boolean isValid = jwtUtil.validateToken(token, userDetails);
        assertTrue(isValid);
    }

    @Test
    public void testValidateToken_UserMismatch() {
        String token = jwtUtil.generateToken("wronguser@example.com", "JOBSEEKER");

        Boolean isValid = jwtUtil.validateToken(token, userDetails);
        assertFalse(isValid);
    }

    @Test
    public void testValidateToken_WithoutUserDetails() {
        String token = jwtUtil.generateToken("testuser@example.com", "JOBSEEKER");

        Boolean isValid = jwtUtil.validateToken(token);
        assertTrue(isValid);
    }

    @Test(expected = io.jsonwebtoken.ExpiredJwtException.class)
    public void testValidateToken_Expired() {
        // Create a JWT Util instance with an extremely short expiration time (e.g.,
        // 1ms)
        JwtUtil expiredJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(expiredJwtUtil, "secret",
                "mySuperSecretKeyThatIsAtLeast32BytesLongForHmacSha256!!!!!!");
        ReflectionTestUtils.setField(expiredJwtUtil, "expiration", -1000L); // Expired 1 second ago

        String token = expiredJwtUtil.generateToken("testuser@example.com", "JOBSEEKER");

        Boolean isValid = jwtUtil.validateToken(token, userDetails);
        assertFalse(isValid);
    }
}
