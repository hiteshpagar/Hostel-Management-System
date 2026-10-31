package com.hms.modules.auth.controller;

import com.hms.common.dto.ApiResponse;
import com.hms.modules.auth.dto.ChangePasswordRequest;
import com.hms.modules.auth.dto.LoginRequest;
import com.hms.modules.auth.dto.LoginResponse;
import com.hms.modules.auth.dto.UserSummaryDto;
import com.hms.modules.auth.service.AuthService;
import com.hms.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {
        if (currentUser == null) {
            throw new com.hms.common.exception.UnauthorizedException("User is not authenticated");
        }
        authService.changePassword(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserSummaryDto>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new com.hms.common.exception.UnauthorizedException("User is not authenticated");
        }
        UserSummaryDto userSummary = authService.getCurrentUser(currentUser);
        return ResponseEntity.ok(ApiResponse.ok(userSummary));
    }
}
