package com.group1.MockProject.service;

import com.group1.MockProject.dto.MessageDTO;
import com.group1.MockProject.dto.request.SignInRequest;
import com.group1.MockProject.dto.response.AdminDashboardResponse;
import com.group1.MockProject.dto.response.SignInResponse;
import com.group1.MockProject.entity.User;
import java.util.List;

public interface AdminService {
  AdminDashboardResponse getDashboardData();

  List<User> getAllUsers();

  MessageDTO setRejectInstructor(int userId);

  MessageDTO setApproveInstructor(int userId);

  SignInResponse authenticate(SignInRequest request);

  void deleteUser(int userId);
}
