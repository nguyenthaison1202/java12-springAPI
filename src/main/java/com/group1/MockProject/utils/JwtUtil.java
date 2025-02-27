package com.group1.MockProject.utils;

import com.group1.MockProject.dto.request.UpdateProfileRequest;
import com.group1.MockProject.entity.User;
// Import Enum UserRole
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  // Secret key nên được lấy từ một nơi bảo mật, ví dụ: từ application.properties, môi trường, hoặc
  // vault
  private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);
  private static final long EXPIRATION_TIME = 86400000; // 1 day

  // Phương thức tạo JWT token, thêm cả vai trò vào payload
  public static String generateToken(User user) {
    return Jwts.builder()
        .setSubject(user.getEmail())
        .claim("role", user.getRole().name())
//        .claim("role", user.getRole())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
        .signWith(SECRET_KEY)
        .compact();
  }

  public static Claims decodeToken(String token) {
    return Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token).getBody();
  }

  public static boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public static String extractEmail(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(SECRET_KEY)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

  private static Claims extractClaims(String token) {
    try {
      return Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token).getBody();
    } catch (Exception e) {
      return null;
    }
  }

  public static String extractRoleFromTokenCourse(String token) {
    Claims claims = extractClaims(token);

    // Kiểm tra xem role có tồn tại trong claims không
    if (claims != null && claims.containsKey("role")) {
      return claims.get("role", String.class);
    }

    // Trả về giá trị mặc định hoặc ném ngoại lệ nếu không tìm thấy role
    return null; // Hoặc có thể ném một RuntimeException nếu cần thiết
  }

  public static String generateUpdateUserToken(String email, UpdateProfileRequest request) {
    return Jwts.builder()
        .setSubject(email)
        .claim("fullName", request.getFullName())
        .claim("phone", request.getPhone())
        .claim("address", request.getAddress())
        .setExpiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000))
        .signWith(SECRET_KEY)
        .compact();
  }

  public static String generateForgotPasswordToken(String email) {
    return Jwts.builder()
        .setSubject(email + "_" + UUID.randomUUID())
        .claim("role", "ROLE_FORGOT_PASSWORD")
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
        .signWith(SECRET_KEY)
        .compact();
  }

  public static String extractEmailFromForgotPasswordToken(String token) {
    String extractStringFromToken =
        Jwts.parserBuilder()
            .setSigningKey(SECRET_KEY)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();

    return extractStringFromToken.split("_")[0];
  }
  private static SecretKey getSigninKey(){
    byte [] keyBytes = Decoders.BASE64.decode(String.valueOf(SECRET_KEY));
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
