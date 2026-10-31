package com.hms.modules.student.service;

import com.hms.common.enums.AllocationStatus;
import com.hms.common.enums.BedStatus;
import com.hms.common.enums.RoleType;
import com.hms.common.enums.UserStatus;
import com.hms.common.exception.BadRequestException;
import com.hms.common.exception.ResourceNotFoundException;
import com.hms.entity.*;
import com.hms.modules.student.dto.*;
import com.hms.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudentServiceImpl implements StudentService {

    private final HostelRepository hostelRepository;
    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final BedRepository bedRepository;
    private final RoomAllocationRepository roomAllocationRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentServiceImpl(HostelRepository hostelRepository,
                              UserRepository userRepository,
                              StudentProfileRepository studentProfileRepository,
                              BedRepository bedRepository,
                              RoomAllocationRepository roomAllocationRepository,
                              PasswordEncoder passwordEncoder) {
        this.hostelRepository = hostelRepository;
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.bedRepository = bedRepository;
        this.roomAllocationRepository = roomAllocationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public StudentDetailResponse onboardStudent(Long hostelId, OnboardStudentRequest request, Long allocatedByUserId) {
        Hostel hostel = hostelRepository.findById(hostelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel", "id", hostelId));

        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("A user with email '" + email + "' is already registered");
        }

        String admissionNo = request.getAdmissionNumber().trim();
        if (studentProfileRepository.existsByHostelIdAndAdmissionNumber(hostelId, admissionNo)) {
            throw new BadRequestException("A student with admission number '" + admissionNo + "' already exists in this hostel");
        }

        String rawPassword = StringUtils.hasText(request.getTemporaryPassword())
                ? request.getTemporaryPassword()
                : "Student@123";

        // 1. Create User account
        User user = new User();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(email);
        user.setPhoneNumber(request.getPhoneNumber().trim());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(RoleType.ROLE_STUDENT);
        user.setMustChangePassword(true);
        user.setStatus(UserStatus.ACTIVE);
        user.setHostel(hostel);
        User savedUser = userRepository.save(user);

        // 2. Create StudentProfile
        StudentProfile profile = new StudentProfile();
        profile.setUser(savedUser);
        profile.setHostel(hostel);
        profile.setAdmissionNumber(admissionNo);
        profile.setCollegeName(request.getCollegeName());
        profile.setDepartment(request.getDepartment());
        profile.setYearOfStudy(request.getYearOfStudy());
        profile.setGuardianName(request.getGuardianName());
        profile.setGuardianPhone(request.getGuardianPhone());
        profile.setGuardianRelation(request.getGuardianRelation());
        profile.setEmergencyContact(request.getEmergencyContact());
        profile.setPermanentAddress(request.getPermanentAddress());
        profile.setBloodGroup(request.getBloodGroup());
        StudentProfile savedProfile = studentProfileRepository.save(profile);

        // 3. Optional initial Bed Allocation
        if (request.getBedId() != null) {
            Bed bed = bedRepository.findById(request.getBedId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bed", "id", request.getBedId()));

            if (!bed.getHostel().getId().equals(hostelId)) {
                throw new BadRequestException("Bed does not belong to the student's assigned hostel");
            }

            if (bed.getStatus() != BedStatus.AVAILABLE) {
                throw new BadRequestException("Selected bed (" + bed.getBedLabel() + ") is currently " + bed.getStatus());
            }

            bed.setStatus(BedStatus.OCCUPIED);
            bedRepository.save(bed);

            User allocatedBy = allocatedByUserId != null
                    ? userRepository.findById(allocatedByUserId).orElse(null)
                    : null;

            LocalDate startDate = request.getAllocationStartDate() != null
                    ? request.getAllocationStartDate()
                    : LocalDate.now();

            RoomAllocation allocation = new RoomAllocation();
            allocation.setHostel(hostel);
            allocation.setStudent(savedProfile);
            allocation.setBed(bed);
            allocation.setAllocatedBy(allocatedBy);
            allocation.setStartDate(startDate);
            allocation.setStatus(AllocationStatus.ACTIVE);
            allocation.setNotes(request.getAllocationNotes());
            roomAllocationRepository.save(allocation);
        }

        return mapToDetailResponse(savedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentSummaryResponse> getStudentsByHostel(Long hostelId, String search, String department) {
        if (!hostelRepository.existsById(hostelId)) {
            throw new ResourceNotFoundException("Hostel", "id", hostelId);
        }

        List<StudentProfile> profiles = studentProfileRepository.findByHostelId(hostelId);

        return profiles.stream()
                .filter(p -> {
                    if (!StringUtils.hasText(search)) return true;
                    String q = search.trim().toLowerCase();
                    String fullName = (p.getUser().getFirstName() + " " + p.getUser().getLastName()).toLowerCase();
                    return fullName.contains(q)
                            || p.getAdmissionNumber().toLowerCase().contains(q)
                            || p.getUser().getEmail().toLowerCase().contains(q)
                            || p.getUser().getPhoneNumber().contains(q);
                })
                .filter(p -> {
                    if (!StringUtils.hasText(department)) return true;
                    return p.getDepartment() != null && p.getDepartment().equalsIgnoreCase(department.trim());
                })
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StudentDetailResponse getStudentById(Long studentId) {
        StudentProfile profile = studentProfileRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("StudentProfile", "id", studentId));
        return mapToDetailResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentDetailResponse getStudentByUserId(Long userId) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user id: " + userId));
        return mapToDetailResponse(profile);
    }

    @Override
    @Transactional
    public StudentDetailResponse updateStudent(Long studentId, UpdateStudentRequest request) {
        StudentProfile profile = studentProfileRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("StudentProfile", "id", studentId));

        User user = profile.getUser();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setPhoneNumber(request.getPhoneNumber().trim());
        userRepository.save(user);

        profile.setCollegeName(request.getCollegeName());
        profile.setDepartment(request.getDepartment());
        profile.setYearOfStudy(request.getYearOfStudy());
        profile.setGuardianName(request.getGuardianName());
        profile.setGuardianPhone(request.getGuardianPhone());
        profile.setGuardianRelation(request.getGuardianRelation());
        profile.setEmergencyContact(request.getEmergencyContact());
        profile.setPermanentAddress(request.getPermanentAddress());
        profile.setBloodGroup(request.getBloodGroup());

        StudentProfile updated = studentProfileRepository.save(profile);
        return mapToDetailResponse(updated);
    }

    @Override
    @Transactional
    public StudentDetailResponse allocateBed(Long studentId, AllocateBedRequest request, Long allocatedByUserId) {
        StudentProfile profile = studentProfileRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("StudentProfile", "id", studentId));

        Optional<RoomAllocation> activeOpt = roomAllocationRepository.findByStudentIdAndStatus(studentId, AllocationStatus.ACTIVE);
        if (activeOpt.isPresent()) {
            throw new BadRequestException("Student already has an active allocation. Use the transfer endpoint to move to a new bed.");
        }

        Bed bed = bedRepository.findById(request.getBedId())
                .orElseThrow(() -> new ResourceNotFoundException("Bed", "id", request.getBedId()));

        if (!bed.getHostel().getId().equals(profile.getHostel().getId())) {
            throw new BadRequestException("Bed does not belong to the student's assigned hostel");
        }

        if (bed.getStatus() != BedStatus.AVAILABLE) {
            throw new BadRequestException("Bed is currently " + bed.getStatus());
        }

        bed.setStatus(BedStatus.OCCUPIED);
        bedRepository.save(bed);

        User allocatedBy = allocatedByUserId != null
                ? userRepository.findById(allocatedByUserId).orElse(null)
                : null;

        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();

        RoomAllocation allocation = new RoomAllocation();
        allocation.setHostel(profile.getHostel());
        allocation.setStudent(profile);
        allocation.setBed(bed);
        allocation.setAllocatedBy(allocatedBy);
        allocation.setStartDate(startDate);
        allocation.setStatus(AllocationStatus.ACTIVE);
        allocation.setNotes(request.getNotes());
        roomAllocationRepository.save(allocation);

        return mapToDetailResponse(profile);
    }

    @Override
    @Transactional
    public StudentDetailResponse transferBed(Long studentId, TransferBedRequest request, Long allocatedByUserId) {
        StudentProfile profile = studentProfileRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("StudentProfile", "id", studentId));

        RoomAllocation currentAllocation = roomAllocationRepository.findByStudentIdAndStatus(studentId, AllocationStatus.ACTIVE)
                .orElseThrow(() -> new BadRequestException("Student does not have an active bed allocation to transfer from"));

        if (currentAllocation.getBed().getId().equals(request.getNewBedId())) {
            throw new BadRequestException("Cannot transfer to the same bed");
        }

        Bed newBed = bedRepository.findById(request.getNewBedId())
                .orElseThrow(() -> new ResourceNotFoundException("Bed", "id", request.getNewBedId()));

        if (!newBed.getHostel().getId().equals(profile.getHostel().getId())) {
            throw new BadRequestException("New bed does not belong to the student's assigned hostel");
        }

        if (newBed.getStatus() != BedStatus.AVAILABLE) {
            throw new BadRequestException("New bed is currently " + newBed.getStatus());
        }

        LocalDate transferDate = request.getTransferDate() != null ? request.getTransferDate() : LocalDate.now();

        // 1. Terminate current allocation & free old bed
        currentAllocation.setStatus(AllocationStatus.TRANSFERRED);
        currentAllocation.setEndDate(transferDate);
        roomAllocationRepository.save(currentAllocation);

        Bed oldBed = currentAllocation.getBed();
        oldBed.setStatus(BedStatus.AVAILABLE);
        bedRepository.save(oldBed);

        // 2. Mark new bed occupied & create new allocation
        newBed.setStatus(BedStatus.OCCUPIED);
        bedRepository.save(newBed);

        User allocatedBy = allocatedByUserId != null
                ? userRepository.findById(allocatedByUserId).orElse(null)
                : null;

        RoomAllocation newAllocation = new RoomAllocation();
        newAllocation.setHostel(profile.getHostel());
        newAllocation.setStudent(profile);
        newAllocation.setBed(newBed);
        newAllocation.setAllocatedBy(allocatedBy);
        newAllocation.setStartDate(transferDate);
        newAllocation.setStatus(AllocationStatus.ACTIVE);
        newAllocation.setNotes(request.getNotes());
        roomAllocationRepository.save(newAllocation);

        return mapToDetailResponse(profile);
    }

    @Override
    @Transactional
    public StudentDetailResponse vacateBed(Long studentId, VacateBedRequest request) {
        StudentProfile profile = studentProfileRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("StudentProfile", "id", studentId));

        RoomAllocation currentAllocation = roomAllocationRepository.findByStudentIdAndStatus(studentId, AllocationStatus.ACTIVE)
                .orElseThrow(() -> new BadRequestException("Student does not have an active bed allocation to vacate"));

        LocalDate vacateDate = request != null && request.getVacateDate() != null
                ? request.getVacateDate()
                : LocalDate.now();

        currentAllocation.setStatus(AllocationStatus.VACATED);
        currentAllocation.setEndDate(vacateDate);
        if (request != null && StringUtils.hasText(request.getNotes())) {
            currentAllocation.setNotes(currentAllocation.getNotes() + " | Vacated: " + request.getNotes());
        }
        roomAllocationRepository.save(currentAllocation);

        Bed bed = currentAllocation.getBed();
        bed.setStatus(BedStatus.AVAILABLE);
        bedRepository.save(bed);

        return mapToDetailResponse(profile);
    }

    // ==========================================
    // MAPPERS
    // ==========================================

    private StudentSummaryResponse mapToSummaryResponse(StudentProfile profile) {
        StudentSummaryResponse response = new StudentSummaryResponse();
        populateSummaryFields(response, profile);
        return response;
    }

    private StudentDetailResponse mapToDetailResponse(StudentProfile profile) {
        StudentDetailResponse response = new StudentDetailResponse();
        populateSummaryFields(response, profile);

        response.setGuardianName(profile.getGuardianName());
        response.setGuardianPhone(profile.getGuardianPhone());
        response.setGuardianRelation(profile.getGuardianRelation());
        response.setEmergencyContact(profile.getEmergencyContact());
        response.setPermanentAddress(profile.getPermanentAddress());
        response.setBloodGroup(profile.getBloodGroup());

        // Load active allocation if present
        Optional<RoomAllocation> activeOpt = roomAllocationRepository.findByStudentIdAndStatus(profile.getId(), AllocationStatus.ACTIVE);
        if (activeOpt.isPresent()) {
            RoomAllocation alloc = activeOpt.get();
            response.setAllocationId(alloc.getId());
            response.setBedId(alloc.getBed().getId());
            response.setRoomId(alloc.getBed().getRoom().getId());
            response.setBlockId(alloc.getBed().getRoom().getBlock().getId());
            response.setRoomType(alloc.getBed().getRoom().getRoomType());
            response.setFloor(alloc.getBed().getRoom().getFloor());
            response.setMonthlyRent(alloc.getBed().getRoom().getMonthlyRent());
            response.setAllocationStartDate(alloc.getStartDate());
            response.setAllocationNotes(alloc.getNotes());
            if (alloc.getAllocatedBy() != null) {
                response.setAllocatedByName(alloc.getAllocatedBy().getFirstName() + " " + alloc.getAllocatedBy().getLastName());
            }
        }

        // Load allocation history
        List<AllocationHistoryResponse> history = roomAllocationRepository.findByStudentId(profile.getId()).stream()
                .sorted(Comparator.comparing(RoomAllocation::getStartDate).reversed())
                .map(a -> new AllocationHistoryResponse(
                        a.getId(),
                        a.getBed().getId(),
                        a.getBed().getBedLabel(),
                        a.getBed().getRoom().getRoomNumber(),
                        a.getBed().getRoom().getBlock().getName(),
                        a.getStartDate(),
                        a.getEndDate(),
                        a.getStatus(),
                        a.getNotes()
                ))
                .collect(Collectors.toList());

        response.setAllocationHistory(history);
        return response;
    }

    private void populateSummaryFields(StudentSummaryResponse response, StudentProfile profile) {
        response.setStudentId(profile.getId());
        response.setUserId(profile.getUser().getId());
        response.setHostelId(profile.getHostel().getId());
        response.setFullName(profile.getUser().getFirstName() + " " + profile.getUser().getLastName());
        response.setEmail(profile.getUser().getEmail());
        response.setPhoneNumber(profile.getUser().getPhoneNumber());
        response.setAdmissionNumber(profile.getAdmissionNumber());
        response.setCollegeName(profile.getCollegeName());
        response.setDepartment(profile.getDepartment());
        response.setYearOfStudy(profile.getYearOfStudy());
        response.setCreatedAt(profile.getCreatedAt());

        Optional<RoomAllocation> activeOpt = roomAllocationRepository.findByStudentIdAndStatus(profile.getId(), AllocationStatus.ACTIVE);
        if (activeOpt.isPresent()) {
            RoomAllocation alloc = activeOpt.get();
            response.setBlockName(alloc.getBed().getRoom().getBlock().getName());
            response.setRoomNumber(alloc.getBed().getRoom().getRoomNumber());
            response.setBedLabel(alloc.getBed().getBedLabel());
            response.setAllocationStatus(AllocationStatus.ACTIVE);
        } else {
            response.setAllocationStatus(AllocationStatus.VACATED);
        }
    }
}
