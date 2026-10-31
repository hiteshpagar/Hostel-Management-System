package com.hms.modules.student.controller;

import com.hms.common.dto.ApiResponse;
import com.hms.modules.student.dto.*;
import com.hms.modules.student.service.StudentService;
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
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // ==========================================
    // ONBOARDING & LISTING (HOSTEL SCOPED)
    // ==========================================

    @PostMapping("/hostels/{hostelId}/students")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSTEL_ADMIN', 'WARDEN')")
    public ResponseEntity<ApiResponse<StudentDetailResponse>> onboardStudent(
            @PathVariable Long hostelId,
            @Valid @RequestBody OnboardStudentRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        validateHostelAccess(currentUser, hostelId);

        StudentDetailResponse response = studentService.onboardStudent(hostelId, request, currentUser.getId());
        return new ResponseEntity<>(ApiResponse.ok("Student onboarded successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/hostels/{hostelId}/students")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSTEL_ADMIN', 'WARDEN')")
    public ResponseEntity<ApiResponse<List<StudentSummaryResponse>>> getStudentsByHostel(
            @PathVariable Long hostelId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String department,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        validateHostelAccess(currentUser, hostelId);

        List<StudentSummaryResponse> students = studentService.getStudentsByHostel(hostelId, search, department);
        return ResponseEntity.ok(ApiResponse.ok(students));
    }

    // ==========================================
    // LOGGED-IN STUDENT SELF-SERVICE
    // ==========================================

    @GetMapping("/students/my-profile")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentDetailResponse>> getMyProfile(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        StudentDetailResponse response = studentService.getStudentByUserId(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ==========================================
    // INDIVIDUAL STUDENT MANAGEMENT
    // ==========================================

    @GetMapping("/students/{studentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSTEL_ADMIN', 'WARDEN')")
    public ResponseEntity<ApiResponse<StudentDetailResponse>> getStudentById(
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        StudentDetailResponse student = studentService.getStudentById(studentId);
        validateHostelAccess(currentUser, student.getHostelId());
        return ResponseEntity.ok(ApiResponse.ok(student));
    }

    @PutMapping("/students/{studentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSTEL_ADMIN', 'WARDEN')")
    public ResponseEntity<ApiResponse<StudentDetailResponse>> updateStudent(
            @PathVariable Long studentId,
            @Valid @RequestBody UpdateStudentRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        StudentDetailResponse student = studentService.getStudentById(studentId);
        validateHostelAccess(currentUser, student.getHostelId());

        StudentDetailResponse updated = studentService.updateStudent(studentId, request);
        return ResponseEntity.ok(ApiResponse.ok("Student profile updated successfully", updated));
    }

    // ==========================================
    // BED ALLOCATION LIFECYCLE
    // ==========================================

    @PostMapping("/students/{studentId}/allocate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSTEL_ADMIN', 'WARDEN')")
    public ResponseEntity<ApiResponse<StudentDetailResponse>> allocateBed(
            @PathVariable Long studentId,
            @Valid @RequestBody AllocateBedRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        StudentDetailResponse student = studentService.getStudentById(studentId);
        validateHostelAccess(currentUser, student.getHostelId());

        StudentDetailResponse response = studentService.allocateBed(studentId, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Bed allocated successfully", response));
    }

    @PostMapping("/students/{studentId}/transfer")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSTEL_ADMIN', 'WARDEN')")
    public ResponseEntity<ApiResponse<StudentDetailResponse>> transferBed(
            @PathVariable Long studentId,
            @Valid @RequestBody TransferBedRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        StudentDetailResponse student = studentService.getStudentById(studentId);
        validateHostelAccess(currentUser, student.getHostelId());

        StudentDetailResponse response = studentService.transferBed(studentId, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Bed transferred successfully", response));
    }

    @PostMapping("/students/{studentId}/vacate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSTEL_ADMIN', 'WARDEN')")
    public ResponseEntity<ApiResponse<StudentDetailResponse>> vacateBed(
            @PathVariable Long studentId,
            @RequestBody(required = false) VacateBedRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        StudentDetailResponse student = studentService.getStudentById(studentId);
        validateHostelAccess(currentUser, student.getHostelId());

        StudentDetailResponse response = studentService.vacateBed(studentId, request);
        return ResponseEntity.ok(ApiResponse.ok("Bed vacated successfully", response));
    }

    // ==========================================
    // TENANT ISOLATION GUARD
    // ==========================================

    private void validateHostelAccess(UserPrincipal currentUser, Long targetHostelId) {
        boolean isSuperAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (!isSuperAdmin) {
            if (currentUser.getHostelId() == null || !currentUser.getHostelId().equals(targetHostelId)) {
                throw new com.hms.common.exception.UnauthorizedException(
                        "Access denied: You are not authorized to view or manage students in this hostel"
                );
            }
        }
    }
}
