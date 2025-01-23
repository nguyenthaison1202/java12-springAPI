package com.group1.MockProject.utils;

import static org.junit.jupiter.api.Assertions.*;

import com.group1.MockProject.dto.request.UpdateProfileRequest;
import com.group1.MockProject.entity.User;
import com.group1.MockProject.entity.UserRole;
import io.jsonwebtoken.Claims;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

  @Mock private User mockUser;

  @Mock private UpdateProfileRequest mockUpdateRequest;

  private static final String MOCK_EMAIL = "test@example.com";
  private static final String MOCK_ROLE = "STUDENT";
  private static final String MOCK_FULL_NAME = "Test User";
  private static final String MOCK_PHONE = "1234567890";
  private static final String MOCK_ADDRESS = "Test Address";

  @BeforeEach
  void setUp() {
    mockUser = new User();
    mockUser.setEmail(MOCK_EMAIL);
    mockUser.setRole(UserRole.STUDENT);
    mockUser.setFullName(MOCK_FULL_NAME);
    mockUser.setPhone(MOCK_PHONE);
    mockUser.setAddress(MOCK_ADDRESS);

    mockUpdateRequest = new UpdateProfileRequest();
    mockUpdateRequest.setAddress(MOCK_ADDRESS);
    mockUpdateRequest.setFullName(MOCK_FULL_NAME);
    mockUpdateRequest.setPhone(MOCK_PHONE);
  }

  @Test
  void testGenerateToken_Success() {
    String token = JwtUtil.generateToken(mockUser);

    assertNotNull(token);
    assertFalse(token.isEmpty());
    assertEquals(MOCK_EMAIL, JwtUtil.extractEmail(token));
  }

  @Test
  void testValidateToken_ShouldReturnTrue() {
    String token = JwtUtil.generateToken(mockUser);

    assertTrue(JwtUtil.validateToken(token));
  }

  @Test
  void testValidateToken_ShouldReturnFalse() {
    // Arrange
    String invalidToken = "invalid.token.string";

    // Act & Assert
    assertFalse(JwtUtil.validateToken(invalidToken));
  }

  @Test
  void testDecodeToken_ShouldReturnValidClaims() {
    // Arrange
    String token = JwtUtil.generateToken(mockUser);

    // Act
    Claims claims = JwtUtil.decodeToken(token);

    // Assert
    assertNotNull(claims);
    assertEquals(MOCK_EMAIL, claims.getSubject());
    assertEquals(MOCK_ROLE, claims.get("role", String.class));
    assertNotNull(claims.getIssuedAt());
    assertNotNull(claims.getExpiration());
  }

  @Test
  void testExtractEmail_ShouldReturnCorrectEmail() {
    // Arrange
    String token = JwtUtil.generateToken(mockUser);

    // Act
    String extractedEmail = JwtUtil.extractEmail(token);

    // Assert
    assertEquals(MOCK_EMAIL, extractedEmail);
  }

  @Test
  void testExtractRoleFromTokenCourse_ShouldReturnRole() {
    // Arrange
    String token = JwtUtil.generateToken(mockUser);

    // Act
    String extractedRole = JwtUtil.extractRoleFromTokenCourse(token);

    // Assert
    assertEquals(MOCK_ROLE, extractedRole);
  }

  @Test
  void testExtractRoleFromTokenCourse_ShouldReturnNull() {
    // Arrange
    String invalidToken = "invalid.token.string";

    // Act
    String extractedRole = JwtUtil.extractRoleFromTokenCourse(invalidToken);

    // Assert
    assertNull(extractedRole);
  }

  @Test
  void testGenerateUpdateUserToken_ShouldCreateValidToken() {
    // Act
    String token = JwtUtil.generateUpdateUserToken(MOCK_EMAIL, mockUpdateRequest);

    // Assert
    assertNotNull(token);
    assertTrue(JwtUtil.validateToken(token));

    Claims claims = JwtUtil.decodeToken(token);
    assertEquals(MOCK_EMAIL, claims.getSubject());
    assertEquals(MOCK_FULL_NAME, claims.get("fullName", String.class));
    assertEquals(MOCK_PHONE, claims.get("phone", String.class));
    assertEquals(MOCK_ADDRESS, claims.get("address", String.class));

    // Verify token expiration is set to 15 minutes from creation
    Date expiration = claims.getExpiration();
    long timeDiff = expiration.getTime() - System.currentTimeMillis();
    assertTrue(timeDiff <= 15 * 60 * 1000 && timeDiff > 14 * 60 * 1000);
  }

  @Test
  void testTokenExpiration_ShouldBeSetToOneDay() {
    // Arrange
    String token = JwtUtil.generateToken(mockUser);
    Claims claims = JwtUtil.decodeToken(token);

    // Act
    Date expiration = claims.getExpiration();
    long timeDiff = expiration.getTime() - claims.getIssuedAt().getTime();

    // Assert
    assertEquals(86400000, timeDiff); // 24 hours in milliseconds
  }
}
