package com.hms.modules.hostel.service;

import com.hms.modules.hostel.dto.*;

import java.util.List;

public interface HostelService {

    HostelResponse createHostel(CreateHostelRequest request);

    List<HostelResponse> getAllHostels();

    HostelResponse getHostelById(Long hostelId);

    HostelResponse updateHostel(Long hostelId, UpdateHostelRequest request);

    StaffResponse addStaff(Long hostelId, CreateStaffRequest request);

    List<StaffResponse> getHostelStaff(Long hostelId);
}
