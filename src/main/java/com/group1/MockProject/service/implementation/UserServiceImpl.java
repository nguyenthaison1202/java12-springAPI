package com.group1.MockProject.service.implementation;

import com.group1.MockProject.dto.request.UpdateProfileRequest;
import com.group1.MockProject.dto.response.UpdateProfileResponse;
import com.group1.MockProject.dto.response.UserInfoResponse;
import com.group1.MockProject.entity.User;
import com.group1.MockProject.repository.UserRepository;
import com.group1.MockProject.service.EmailService;
import com.group1.MockProject.service.UserService;
import com.group1.MockProject.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import java.util.Optional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private ModelMapper mapper;
  private EmailService emailService;

  @Value("${APPLICATION_HOST}")
  private String HOST;

  public UserServiceImpl(
      UserRepository userRepository, ModelMapper mapper, EmailService emailService) {
    this.userRepository = userRepository;
    this.mapper = mapper;
    this.emailService = emailService;
  }

  @Override
  public UserInfoResponse getUserInfoByToken(String token) {

    String email = JwtUtil.extractEmail(token);

    Optional<User> existedUser = userRepository.findByEmail(email);
    if (existedUser.isEmpty()) {
      throw new EmptyResultDataAccessException("Người dùng không tồn tại", 1);
    }

    return mapper.map(existedUser, UserInfoResponse.class);
  }

  @Override
  public UpdateProfileResponse requestProfileUpdate(String token, UpdateProfileRequest request) {
    String email = JwtUtil.extractEmail(token);

    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new EmptyResultDataAccessException("Không tìm thấy người dùng", 1));

    String updateUserToken = JwtUtil.generateUpdateUserToken(email, request);
    String message = "Đã gửi link cập nhật profile thành công";
    String link = HOST + "/api/v1/profile/confirm-update?token=" + updateUserToken;

    String title = "Cập nhật thông tin cá nhân";
    String content =
        "Vui lòng nhấn <blockquote style=\"Margin: 0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\"><a href=\""
            + link
            + "\">vào đây.</a> để cập nhật thông tin cá nhân của bạn.</blockquote>";

    emailService.sendDetail(
        user.getEmail(),
        emailService.buildEmail(title, user.getEmail(), content),
        "Cập nhật thông tin cá nhân");

    //        emailService.sendDetail(user.getEmail(),buildUpdateRequestEmail(user.getEmail(), link)
    // , "Cập nhật thông tin cá nhân");
    return new UpdateProfileResponse(message, link);
  }

  @Override
  public UserInfoResponse updateUserInfo(String token) {

    Claims claims = JwtUtil.decodeToken(token);

    User user =
        userRepository
            .findByEmail(claims.getSubject())
            .orElseThrow(() -> new EmptyResultDataAccessException("Không tìm thấy người dùng", 1));
    user.setFullName(claims.get("fullName", String.class));
    user.setPhone(claims.get("phone", String.class));
    user.setAddress(claims.get("address", String.class));

    User updateUser = userRepository.save(user);

    return mapper.map(updateUser, UserInfoResponse.class);
  }

  @Override
  public void blockUserById(int id) {
    User user =
            userRepository
                    .findById(id)
                    .orElseThrow(() -> new EmptyResultDataAccessException("Không tìm thấy người dùng", 1));
    user.setStatus(-1);
    userRepository.save(user);
  }

  @Override
  public void unblockUserById(int id) {
    User user =
            userRepository
                    .findById(id)
                    .orElseThrow(() -> new EmptyResultDataAccessException("Không tìm thấy người dùng", 1));
    user.setStatus(1);
    userRepository.save(user);
  }
}
