package com.hms.modules.student.service;

import com.hms.modules.student.dto.*;

import java.util.List;

public interface StudentService {

    StudentDetailResponse onboardStudent(Long hostelId, OnboardStudentRequest request, Long allocatedByUserId);

    List<StudentSummaryResponse> getStudentsByHostel(Long hostelId, String search, String department);

    StudentDetailResponse getStudentById(Long studentId);

    StudentDetailResponse getStudentByUserId(Long userId);

    StudentDetailResponse updateStudent(Long studentId, UpdateStudentRequest request);

    StudentDetailResponse allocateBed(Long studentId, AllocateBedRequest request, Long allocatedByUserId);

    StudentDetailResponse transferBed(Long studentId, TransferBedRequest request, Long allocatedByUserId);

    StudentDetailResponse vacateBed(Long studentId, VacateBedRequest request);
}
