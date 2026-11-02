package com.hms.modules.leave.controller;

import com.hms.common.dto.ApiResponse;
import com.hms.common.enums.LeaveStatus;
import com.hms.common.exception.UnauthorizedException;
import com.hms.modules.leave.dto.ApplyLeaveRequest;
import com.hms.modules.leave.dto.LeaveResponse;
import com.hms.modules.leave.dto.LogReturnRequest;
import com.hms.modules.leave.dto.ReviewLeaveRequest;
import com.hms.modules.leave.service.LeaveService;
import com.hms.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    // ==========================================
    // STUDENT LEAVE ENDPOINTS
    // ==========================================

    @PostMapping("/leaves")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<LeaveResponse>> applyLeave(
            @Valid @RequestBody ApplyLeaveRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        LeaveResponse response = leaveService.applyLeave(currentUser.getId(), request);
        return new ResponseEntity<>(ApiResponse.ok("Leave request submitted successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/leaves/my-leaves")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<LeaveResponse>>> getMyLeaves(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<LeaveResponse> list = leaveService.getMyLeaves(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PutMapping("/leaves/{id}/cancel")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<LeaveResponse>> cancelLeave(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        LeaveResponse response = leaveService.cancelLeave(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Leave request cancelled successfully", response));
    }

    // ==========================================
    // HOSTEL MANAGEMENT & GATE PASS ENDPOINTS
    // ==========================================

    @GetMapping("/hostels/{hostelId}/leaves")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSTEL_ADMIN', 'WARDEN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<LeaveResponse>>> getHostelLeaves(
            @PathVariable Long hostelId,
            @RequestParam(required = false) LeaveStatus status,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        validateHostelAccess(currentUser, hostelId);

        List<LeaveResponse> leaves = leaveService.getHostelLeaves(hostelId, status, search);
        return ResponseEntity.ok(ApiResponse.ok(leaves));
    }

    @GetMapping("/leaves/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSTEL_ADMIN', 'WARDEN', 'STAFF', 'STUDENT')")
    public ResponseEntity<ApiResponse<LeaveResponse>> getLeaveById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        LeaveResponse response = leaveService.getLeaveById(id);

        boolean isStudent = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));

        if (isStudent) {
            if (!response.getUserId().equals(currentUser.getId())) {
                throw new UnauthorizedException("You are not authorized to view another student's leave request");
            }
        } else {
            validateHostelAccess(currentUser, response.getHostelId());
        }

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/leaves/{id}/review")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSTEL_ADMIN', 'WARDEN')")
    public ResponseEntity<ApiResponse<LeaveResponse>> reviewLeave(
            @PathVariable Long id,
            @Valid @RequestBody ReviewLeaveRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        LeaveResponse leave = leaveService.getLeaveById(id);
        validateHostelAccess(currentUser, leave.getHostelId());

        LeaveResponse response = leaveService.reviewLeave(id, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Leave request " + request.getStatus().name().toLowerCase() + " successfully", response));
    }

    @PutMapping("/leaves/{id}/return")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSTEL_ADMIN', 'WARDEN', 'STAFF')")
    public ResponseEntity<ApiResponse<LeaveResponse>> recordReturn(
            @PathVariable Long id,
            @RequestBody(required = false) LogReturnRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        LeaveResponse leave = leaveService.getLeaveById(id);
        validateHostelAccess(currentUser, leave.getHostelId());

        LeaveResponse response = leaveService.recordReturn(id, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Gate return recorded successfully", response));
    }

    // ==========================================
    // TENANT ISOLATION GUARD
    // ==========================================

    private void validateHostelAccess(UserPrincipal currentUser, Long targetHostelId) {
        boolean isSuperAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (!isSuperAdmin) {
            if (currentUser.getHostelId() == null || !currentUser.getHostelId().equals(targetHostelId)) {
                throw new UnauthorizedException("Access denied: You are not authorized to view or manage leaves in this hostel");
            }
        }
    }
}
