package com.group1.MockProject.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VNPayUtilTest {

  @Mock private HttpServletRequest mockRequest;

  @Test
  void hmacSHA512_WithValidInput_ShouldReturnHashedString() {
    String key = "secretKey123";
    String data = "testData123";

    String result = VNPayUtil.hmacSHA512(key, data);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(128, result.length());
  }

  @Test
  void testHmacSHA512_ShouldReturnEmptyString() {
    String key = null;
    String data = "testData";

    String result = VNPayUtil.hmacSHA512(key, data);

    assertEquals("", result);
  }

  @Test
  void testHmacSHA512_WithNullData_ShouldReturnEmptyString() {
    String key = "testKey";
    String data = null;

    String result = VNPayUtil.hmacSHA512(key, data);

    assertEquals("", result);
  }

  @Test
  void testGetIpAddress_ShouldReturnCorrectIP() {
    String expectedIp = "192.168.1.1";
    when(mockRequest.getHeader("X-FORWARDED-FOR")).thenReturn(expectedIp);

    String result = VNPayUtil.getIpAddress(mockRequest);

    assertEquals(expectedIp, result);
    verify(mockRequest).getHeader("X-FORWARDED-FOR");
  }

  @Test
  void testGetIpAddress_WithoutXForwardedFor_ShouldReturnRemoteAddr() {
    String expectedIp = "127.0.0.1";
    when(mockRequest.getHeader("X-FORWARDED-FOR")).thenReturn(null);
    when(mockRequest.getRemoteAddr()).thenReturn(expectedIp);

    String result = VNPayUtil.getIpAddress(mockRequest);

    assertEquals(expectedIp, result);
    verify(mockRequest).getHeader("X-FORWARDED-FOR");
    verify(mockRequest).getRemoteAddr();
  }

  @Test
  void testGetIpAddress_WithException_ShouldReturnErrorMessage() {
    when(mockRequest.getHeader("X-FORWARDED-FOR"))
        .thenThrow(new RuntimeException("Test Exception"));

    String result = VNPayUtil.getIpAddress(mockRequest);

    assertTrue(result.startsWith("Invalid IP:"));
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 5, 10, 15})
  void testGetRandomNumber_ShouldReturnCorrectLength(int length) {
    String result = VNPayUtil.getRandomNumber(length);

    assertEquals(length, result.length());
    assertTrue(result.matches("\\d+"));
  }

  @Test
  void testGetRandomNumber_ShouldGenerateRandomValues() {
    int length = 10;

    String result1 = VNPayUtil.getRandomNumber(length);
    String result2 = VNPayUtil.getRandomNumber(length);

    assertNotEquals(result1, result2);
  }

  @Test
  void testGetPaymentURL_WithValidParams_ShouldReturnCorrectURL() {
    // Arrange
    Map<String, String> params = new HashMap<>();
    params.put("param1", "value1");
    params.put("param2", "value 2");
    params.put("param3", null);
    params.put("param4", "");

    // Act
    String result = VNPayUtil.getPaymentURL(params, true);

    // Assert
    assertTrue(result.contains("param1=value1"));
    assertTrue(result.contains("param2=value+2"));
    assertFalse(result.contains("param3"));
    assertFalse(result.contains("param4"));
  }

  @Test
  void testGetPaymentURL_WithSpecialCharacters_ShouldEncodeCorrectly() {
    Map<String, String> params = new HashMap<>();
    params.put("special", "!@#$%^&*");

    String result = VNPayUtil.getPaymentURL(params, false);

    assertEquals("special=%21%40%23%24%25%5E%26*", result);
  }

  @Test
  void testGetPaymentURL_WithEncodedKeys_ShouldEncodeKeysAndValues() {
    Map<String, String> params = new HashMap<>();
    params.put("key with space", "value with space");

    String result = VNPayUtil.getPaymentURL(params, true);

    assertTrue(result.contains("key+with+space=value+with+space"));
  }

  @Test
  void testGetPaymentURL_WithEmptyMap_ShouldReturnEmptyString() {
    Map<String, String> params = new HashMap<>();

    String result = VNPayUtil.getPaymentURL(params, true);

    assertTrue(result.isEmpty());
  }

  @Test
  void testHmacSHA512_ConsistentResults_ShouldReturnSameHash() {
    String key = "testKey";
    String data = "testData";

    String hash1 = VNPayUtil.hmacSHA512(key, data);
    String hash2 = VNPayUtil.hmacSHA512(key, data);

    assertEquals(hash1, hash2);
  }
}
