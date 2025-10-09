package com.tradingpt.tpt_api.domain.user.service;

import com.tradingpt.tpt_api.domain.user.dto.request.ChangePasswordRequest;
import com.tradingpt.tpt_api.domain.user.dto.response.FindIdResponseDTO;
import com.tradingpt.tpt_api.domain.auth.dto.response.MeResponse;
import com.tradingpt.tpt_api.domain.user.dto.response.ProfileImageResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    void ensureUnique(String username, String email, String phone);
    FindIdResponseDTO findUserId(String email);
    MeResponse getMe(Long userId);
    void changePassword(Long userId, ChangePasswordRequest req);
    void deleteAccount(Long userId);
    ProfileImageResponseDTO updateProfileImage(Long userId, MultipartFile file);
}