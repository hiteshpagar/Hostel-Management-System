package com.hms.modules.leave.service;

import com.hms.common.enums.AllocationStatus;
import com.hms.common.enums.LeaveStatus;
import com.hms.common.exception.BadRequestException;
import com.hms.common.exception.ResourceNotFoundException;
import com.hms.common.exception.UnauthorizedException;
import com.hms.entity.*;
import com.hms.modules.leave.dto.ApplyLeaveRequest;
import com.hms.modules.leave.dto.LeaveResponse;
import com.hms.modules.leave.dto.LogReturnRequest;
import com.hms.modules.leave.dto.ReviewLeaveRequest;
import com.hms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;
    private final HostelRepository hostelRepository;
    private final RoomAllocationRepository roomAllocationRepository;

    public LeaveServiceImpl(LeaveRequestRepository leaveRequestRepository,
                            StudentProfileRepository studentProfileRepository,
                            UserRepository userRepository,
                            HostelRepository hostelRepository,
                            RoomAllocationRepository roomAllocationRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.userRepository = userRepository;
        this.hostelRepository = hostelRepository;
        this.roomAllocationRepository = roomAllocationRepository;
    }

    @Override
    @Transactional
    public LeaveResponse applyLeave(Long studentUserId, ApplyLeaveRequest request) {
        StudentProfile student = studentProfileRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user id: " + studentUserId));

        if (!request.getExpectedReturnTime().isAfter(request.getDepartureTime())) {
            throw new BadRequestException("Expected return time must be strictly after departure time");
        }

        // Allow max 15 minutes grace period in the past for network/clock skew
        if (request.getDepartureTime().isBefore(LocalDateTime.now().minusMinutes(15))) {
            throw new BadRequestException("Departure time cannot be in the past");
        }

        // Check for overlapping active or pending leave applications
        List<LeaveRequest> overlapping = leaveRequestRepository.findOverlappingLeaves(
                student.getId(),
                List.of(LeaveStatus.PENDING, LeaveStatus.APPROVED),
                request.getDepartureTime(),
                request.getExpectedReturnTime()
        );

        if (!overlapping.isEmpty()) {
            throw new BadRequestException("You already have an active or pending leave request covering this time window");
        }

        LeaveRequest leave = new LeaveRequest();
        leave.setHostel(student.getHostel());
        leave.setStudent(student);
        leave.setLeaveType(request.getLeaveType());
        leave.setDepartureTime(request.getDepartureTime());
        leave.setExpectedReturnTime(request.getExpectedReturnTime());
        leave.setReason(request.getReason().trim());
        leave.setStatus(LeaveStatus.PENDING);

        LeaveRequest saved = leaveRequestRepository.save(leave);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public LeaveResponse reviewLeave(Long leaveId, ReviewLeaveRequest request, Long reviewerUserId) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", leaveId));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Only PENDING leave requests can be reviewed. Current status is " + leave.getStatus());
        }

        if (request.getStatus() != LeaveStatus.APPROVED && request.getStatus() != LeaveStatus.REJECTED) {
            throw new BadRequestException("Review decision must be either APPROVED or REJECTED");
        }

        if (request.getStatus() == LeaveStatus.REJECTED && !StringUtils.hasText(request.getRejectionReason())) {
            throw new BadRequestException("Rejection reason is required when rejecting a leave request");
        }

        User reviewer = reviewerUserId != null
                ? userRepository.findById(reviewerUserId).orElse(null)
                : null;

        leave.setStatus(request.getStatus());
        leave.setApprovedBy(reviewer);
        if (request.getStatus() == LeaveStatus.REJECTED) {
            leave.setRejectionReason(request.getRejectionReason().trim());
        }

        LeaveRequest updated = leaveRequestRepository.save(leave);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public LeaveResponse recordReturn(Long leaveId, LogReturnRequest request, Long staffUserId) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", leaveId));

        if (leave.getStatus() != LeaveStatus.APPROVED) {
            throw new BadRequestException("Gate return check-in can only be recorded for APPROVED leaves. Current status is " + leave.getStatus());
        }

        LocalDateTime returnTime = (request != null && request.getActualReturnTime() != null)
                ? request.getActualReturnTime()
                : LocalDateTime.now();

        leave.setActualReturnTime(returnTime);
        leave.setStatus(LeaveStatus.COMPLETED);

        LeaveRequest updated = leaveRequestRepository.save(leave);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public LeaveResponse cancelLeave(Long leaveId, Long studentUserId) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", leaveId));

        StudentProfile student = studentProfileRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user id: " + studentUserId));

        if (!leave.getStudent().getId().equals(student.getId())) {
            throw new UnauthorizedException("You are not authorized to cancel this leave request");
        }

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Only PENDING leave requests can be cancelled. Current status is " + leave.getStatus());
        }

        leave.setStatus(LeaveStatus.CANCELLED);
        LeaveRequest updated = leaveRequestRepository.save(leave);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponse> getHostelLeaves(Long hostelId, LeaveStatus status, String search) {
        if (!hostelRepository.existsById(hostelId)) {
            throw new ResourceNotFoundException("Hostel", "id", hostelId);
        }

        List<LeaveRequest> list = status != null
                ? leaveRequestRepository.findByHostelIdAndStatusOrderByCreatedAtDesc(hostelId, status)
                : leaveRequestRepository.findByHostelIdOrderByCreatedAtDesc(hostelId);

        return list.stream()
                .filter(l -> {
                    if (!StringUtils.hasText(search)) return true;
                    String q = search.trim().toLowerCase();
                    StudentProfile sp = l.getStudent();
                    String fullName = (sp.getUser().getFirstName() + " " + sp.getUser().getLastName()).toLowerCase();
                    return fullName.contains(q)
                            || sp.getAdmissionNumber().toLowerCase().contains(q)
                            || sp.getUser().getEmail().toLowerCase().contains(q)
                            || l.getReason().toLowerCase().contains(q);
                })
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponse> getMyLeaves(Long studentUserId) {
        StudentProfile student = studentProfileRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user id: " + studentUserId));

        return leaveRequestRepository.findByStudentIdOrderByCreatedAtDesc(student.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveResponse getLeaveById(Long leaveId) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", leaveId));
        return mapToResponse(leave);
    }

    // ==========================================
    // MAPPER
    // ==========================================

    private LeaveResponse mapToResponse(LeaveRequest leave) {
        LeaveResponse response = new LeaveResponse();
        response.setId(leave.getId());
        response.setHostelId(leave.getHostel().getId());
        response.setHostelName(leave.getHostel().getName());

        StudentProfile sp = leave.getStudent();
        User u = sp.getUser();
        response.setStudentId(sp.getId());
        response.setUserId(u.getId());
        response.setStudentName(u.getFirstName() + " " + u.getLastName());
        response.setAdmissionNumber(sp.getAdmissionNumber());
        response.setStudentPhone(u.getPhoneNumber());
        response.setStudentEmail(u.getEmail());
        response.setEmergencyContact(sp.getEmergencyContact());

        // Find active bed/room allocation
        Optional<RoomAllocation> activeAlloc = roomAllocationRepository.findByStudentIdAndStatus(sp.getId(), AllocationStatus.ACTIVE);
        activeAlloc.ifPresent(alloc -> {
            Bed bed = alloc.getBed();
            response.setBedLabel(bed.getBedLabel());
            if (bed.getRoom() != null) {
                response.setRoomNumber(bed.getRoom().getRoomNumber());
                if (bed.getRoom().getBlock() != null) {
                    response.setBlockName(bed.getRoom().getBlock().getName());
                }
            }
        });

        response.setLeaveType(leave.getLeaveType());
        response.setDepartureTime(leave.getDepartureTime());
        response.setExpectedReturnTime(leave.getExpectedReturnTime());
        response.setActualReturnTime(leave.getActualReturnTime());
        response.setReason(leave.getReason());
        response.setStatus(leave.getStatus());

        if (leave.getApprovedBy() != null) {
            response.setApprovedByUserId(leave.getApprovedBy().getId());
            response.setApprovedByName(leave.getApprovedBy().getFirstName() + " " + leave.getApprovedBy().getLastName());
        }
        response.setRejectionReason(leave.getRejectionReason());

        response.setCreatedAt(leave.getCreatedAt());
        response.setUpdatedAt(leave.getUpdatedAt());

        return response;
    }
}
