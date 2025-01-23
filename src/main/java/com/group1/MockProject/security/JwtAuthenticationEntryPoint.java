package com.group1.MockProject.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group1.MockProject.dto.error.BasicErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final ObjectMapper objectMapper;

  public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    BasicErrorResponseDto responseDto =
        new BasicErrorResponseDto(
            401, HttpStatus.UNAUTHORIZED.getReasonPhrase(), "Token không hợp lệ hoặc đã hết hạn");

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().write(objectMapper.writeValueAsString(responseDto));
    response.getWriter().flush();
  }
}
