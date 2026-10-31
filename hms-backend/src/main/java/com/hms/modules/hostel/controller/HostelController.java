package com.hms.modules.hostel.controller;

import com.hms.common.dto.ApiResponse;
import com.hms.modules.hostel.dto.*;
import com.hms.modules.hostel.service.HostelService;
import com.hms.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hostels")
public class HostelController {

    private final HostelService hostelService;

    public HostelController(HostelService hostelService) {
        this.hostelService = hostelService;
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<HostelResponse>> createHostel(
            @Valid @RequestBody CreateHostelRequest request) {
        HostelResponse response = hostelService.createHostel(request);
        return new ResponseEntity<>(ApiResponse.ok("Hostel created successfully", response), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<HostelResponse>>> getAllHostels() {
        List<HostelResponse> hostels = hostelService.getAllHostels();
        return ResponseEntity.ok(ApiResponse.ok(hostels));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSTEL_ADMIN', 'WARDEN')")
    public ResponseEntity<ApiResponse<HostelResponse>> getHostelById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        // Multi-tenant check: Non-super-admins can only view their own assigned hostel
        validateHostelAccess(currentUser, id);

        HostelResponse response = hostelService.getHostelById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSTEL_ADMIN')")
    public ResponseEntity<ApiResponse<HostelResponse>> updateHostel(
            @PathVariable Long id,
            @Valid @RequestBody UpdateHostelRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        validateHostelAccess(currentUser, id);

        HostelResponse response = hostelService.updateHostel(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Hostel updated successfully", response));
    }

    @PostMapping("/{id}/staff")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSTEL_ADMIN')")
    public ResponseEntity<ApiResponse<StaffResponse>> addStaff(
            @PathVariable Long id,
            @Valid @RequestBody CreateStaffRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        validateHostelAccess(currentUser, id);

        StaffResponse response = hostelService.addStaff(id, request);
        return new ResponseEntity<>(ApiResponse.ok("Staff member added successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/{id}/staff")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSTEL_ADMIN', 'WARDEN')")
    public ResponseEntity<ApiResponse<List<StaffResponse>>> getHostelStaff(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        validateHostelAccess(currentUser, id);

        List<StaffResponse> staffList = hostelService.getHostelStaff(id);
        return ResponseEntity.ok(ApiResponse.ok(staffList));
    }

    private void validateHostelAccess(UserPrincipal currentUser, Long targetHostelId) {
        boolean isSuperAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (!isSuperAdmin) {
            if (currentUser.getHostelId() == null || !currentUser.getHostelId().equals(targetHostelId)) {
                throw new com.hms.common.exception.UnauthorizedException(
                        "Access denied: You are not authorized to view or modify this hostel"
                );
            }
        }
    }
}
