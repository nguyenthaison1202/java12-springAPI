package com.group1.MockProject.service;

import com.group1.MockProject.dto.request.UpdateProfileRequest;
import com.group1.MockProject.dto.response.UpdateProfileResponse;
import com.group1.MockProject.dto.response.UserInfoResponse;

public interface UserService {

    UserInfoResponse getUserInfoByToken(String token);

    UpdateProfileResponse requestProfileUpdate(String token, UpdateProfileRequest request);

    UserInfoResponse updateUserInfo(String token);

    void blockUserById(int id);

    void unblockUserById(int id);
}
