package com.hms.modules.auth.service;

import com.hms.modules.auth.dto.ChangePasswordRequest;
import com.hms.modules.auth.dto.LoginRequest;
import com.hms.modules.auth.dto.LoginResponse;
import com.hms.modules.auth.dto.UserSummaryDto;
import com.hms.security.UserPrincipal;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    void changePassword(Long userId, ChangePasswordRequest request);
    UserSummaryDto getCurrentUser(UserPrincipal currentUser);
}
