package com.hms.modules.leave.service;

import com.hms.common.enums.LeaveStatus;
import com.hms.modules.leave.dto.ApplyLeaveRequest;
import com.hms.modules.leave.dto.LeaveResponse;
import com.hms.modules.leave.dto.LogReturnRequest;
import com.hms.modules.leave.dto.ReviewLeaveRequest;

import java.util.List;

public interface LeaveService {

    LeaveResponse applyLeave(Long studentUserId, ApplyLeaveRequest request);

    LeaveResponse reviewLeave(Long leaveId, ReviewLeaveRequest request, Long reviewerUserId);

    LeaveResponse recordReturn(Long leaveId, LogReturnRequest request, Long staffUserId);

    LeaveResponse cancelLeave(Long leaveId, Long studentUserId);

    List<LeaveResponse> getHostelLeaves(Long hostelId, LeaveStatus status, String search);

    List<LeaveResponse> getMyLeaves(Long studentUserId);

    LeaveResponse getLeaveById(Long leaveId);
}
