package com.hms.modules.hostel.service;

import com.hms.common.enums.RoleType;
import com.hms.common.enums.UserStatus;
import com.hms.common.exception.BadRequestException;
import com.hms.common.exception.ResourceNotFoundException;
import com.hms.entity.Hostel;
import com.hms.entity.User;
import com.hms.modules.hostel.dto.*;
import com.hms.repository.HostelRepository;
import com.hms.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HostelServiceImpl implements HostelService {

    private final HostelRepository hostelRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public HostelServiceImpl(HostelRepository hostelRepository,
                             UserRepository userRepository,
                             PasswordEncoder passwordEncoder) {
        this.hostelRepository = hostelRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public HostelResponse createHostel(CreateHostelRequest request) {
        String code = request.getCode().trim().toUpperCase();

        if (hostelRepository.existsByCode(code)) {
            throw new BadRequestException("A hostel with code '" + code + "' already exists");
        }

        Hostel hostel = new Hostel();
        hostel.setName(request.getName().trim());
        hostel.setCode(code);
        hostel.setContactEmail(request.getContactEmail() != null ? request.getContactEmail().trim().toLowerCase() : null);
        hostel.setContactPhone(request.getContactPhone());
        hostel.setAddress(request.getAddress());
        hostel.setCity(request.getCity());
        hostel.setState(request.getState());
        hostel.setPostalCode(request.getPostalCode());
        hostel.setStatus("ACTIVE");

        Hostel savedHostel = hostelRepository.save(hostel);

        // Optionally create initial Hostel Admin
        if (StringUtils.hasText(request.getAdminEmail())) {
            String adminEmail = request.getAdminEmail().trim().toLowerCase();
            if (userRepository.existsByEmail(adminEmail)) {
                throw new BadRequestException("User email '" + adminEmail + "' is already in use");
            }

            String rawPassword = StringUtils.hasText(request.getAdminPassword())
                    ? request.getAdminPassword()
                    : "Admin@123";

            User admin = new User();
            admin.setFirstName(StringUtils.hasText(request.getAdminFirstName()) ? request.getAdminFirstName() : "Hostel");
            admin.setLastName(StringUtils.hasText(request.getAdminLastName()) ? request.getAdminLastName() : "Admin");
            admin.setEmail(adminEmail);
            admin.setPhoneNumber(StringUtils.hasText(request.getAdminPhone()) ? request.getAdminPhone() : "N/A");
            admin.setPasswordHash(passwordEncoder.encode(rawPassword));
            admin.setRole(RoleType.ROLE_HOSTEL_ADMIN);
            admin.setMustChangePassword(true);
            admin.setStatus(UserStatus.ACTIVE);
            admin.setHostel(savedHostel);

            userRepository.save(admin);
        }

        return mapToHostelResponse(savedHostel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HostelResponse> getAllHostels() {
        return hostelRepository.findAll().stream()
                .map(this::mapToHostelResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public HostelResponse getHostelById(Long hostelId) {
        Hostel hostel = hostelRepository.findById(hostelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel", "id", hostelId));
        return mapToHostelResponse(hostel);
    }

    @Override
    @Transactional
    public HostelResponse updateHostel(Long hostelId, UpdateHostelRequest request) {
        Hostel hostel = hostelRepository.findById(hostelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel", "id", hostelId));

        hostel.setName(request.getName().trim());
        hostel.setContactEmail(request.getContactEmail() != null ? request.getContactEmail().trim().toLowerCase() : null);
        hostel.setContactPhone(request.getContactPhone());
        hostel.setAddress(request.getAddress());
        hostel.setCity(request.getCity());
        hostel.setState(request.getState());
        hostel.setPostalCode(request.getPostalCode());

        if (StringUtils.hasText(request.getStatus())) {
            hostel.setStatus(request.getStatus().trim().toUpperCase());
        }

        Hostel updated = hostelRepository.save(hostel);
        return mapToHostelResponse(updated);
    }

    @Override
    @Transactional
    public StaffResponse addStaff(Long hostelId, CreateStaffRequest request) {
        Hostel hostel = hostelRepository.findById(hostelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel", "id", hostelId));

        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("User email '" + email + "' is already in use");
        }

        // Validate role cannot be Super Admin or Student for this endpoint
        if (request.getRole() == RoleType.ROLE_SUPER_ADMIN || request.getRole() == RoleType.ROLE_STUDENT) {
            throw new BadRequestException("Invalid staff role. Allowed: ROLE_HOSTEL_ADMIN, ROLE_WARDEN, ROLE_STAFF");
        }

        String rawPassword = StringUtils.hasText(request.getTemporaryPassword())
                ? request.getTemporaryPassword()
                : "Staff@123";

        User staff = new User();
        staff.setFirstName(request.getFirstName().trim());
        staff.setLastName(request.getLastName().trim());
        staff.setEmail(email);
        staff.setPhoneNumber(request.getPhoneNumber().trim());
        staff.setPasswordHash(passwordEncoder.encode(rawPassword));
        staff.setRole(request.getRole());
        staff.setMustChangePassword(true);
        staff.setStatus(UserStatus.ACTIVE);
        staff.setHostel(hostel);

        User saved = userRepository.save(staff);
        return mapToStaffResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffResponse> getHostelStaff(Long hostelId) {
        if (!hostelRepository.existsById(hostelId)) {
            throw new ResourceNotFoundException("Hostel", "id", hostelId);
        }

        return userRepository.findByHostelId(hostelId).stream()
                .filter(u -> u.getRole() != RoleType.ROLE_STUDENT)
                .map(this::mapToStaffResponse)
                .collect(Collectors.toList());
    }

    private HostelResponse mapToHostelResponse(Hostel hostel) {
        HostelResponse response = new HostelResponse();
        response.setId(hostel.getId());
        response.setName(hostel.getName());
        response.setCode(hostel.getCode());
        response.setContactEmail(hostel.getContactEmail());
        response.setContactPhone(hostel.getContactPhone());
        response.setAddress(hostel.getAddress());
        response.setCity(hostel.getCity());
        response.setState(hostel.getState());
        response.setPostalCode(hostel.getPostalCode());
        response.setStatus(hostel.getStatus());
        response.setCreatedAt(hostel.getCreatedAt());
        response.setUpdatedAt(hostel.getUpdatedAt());
        return response;
    }

    private StaffResponse mapToStaffResponse(User user) {
        Long hostelId = user.getHostel() != null ? user.getHostel().getId() : null;
        String hostelName = user.getHostel() != null ? user.getHostel().getName() : null;
        String fullName = user.getFirstName() + " " + user.getLastName();

        return new StaffResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                fullName,
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getStatus(),
                user.isMustChangePassword(),
                hostelId,
                hostelName,
                user.getCreatedAt()
        );
    }
}
