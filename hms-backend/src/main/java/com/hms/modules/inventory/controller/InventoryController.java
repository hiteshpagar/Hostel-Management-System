package com.hms.modules.inventory.controller;

import com.hms.common.dto.ApiResponse;
import com.hms.modules.inventory.dto.*;
import com.hms.modules.inventory.service.InventoryService;
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
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // ==========================================
    // BLOCK ENDPOINTS
    // ==========================================

    @PostMapping("/hostels/{hostelId}/blocks")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSTEL_ADMIN')")
    public ResponseEntity<ApiResponse<BlockResponse>> createBlock(
            @PathVariable Long hostelId,
            @Valid @RequestBody CreateBlockRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        validateHostelAccess(currentUser, hostelId);
        BlockResponse response = inventoryService.createBlock(hostelId, request);
        return new ResponseEntity<>(ApiResponse.ok("Block created successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/hostels/{hostelId}/blocks")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSTEL_ADMIN', 'WARDEN', 'STAFF', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<BlockResponse>>> getBlocksByHostel(
            @PathVariable Long hostelId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        validateHostelAccess(currentUser, hostelId);
        List<BlockResponse> blocks = inventoryService.getBlocksByHostel(hostelId);
        return ResponseEntity.ok(ApiResponse.ok(blocks));
    }

    @GetMapping("/blocks/{blockId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSTEL_ADMIN', 'WARDEN', 'STAFF', 'STUDENT')")
    public ResponseEntity<ApiResponse<BlockResponse>> getBlockById(
            @PathVariable Long blockId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        BlockResponse block = inventoryService.getBlockById(blockId);
        validateHostelAccess(currentUser, block.getHostelId());
        return ResponseEntity.ok(ApiResponse.ok(block));
    }

    @PutMapping("/blocks/{blockId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSTEL_ADMIN')")
    public ResponseEntity<ApiResponse<BlockResponse>> updateBlock(
            @PathVariable Long blockId,
            @Valid @RequestBody UpdateBlockRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        BlockResponse block = inventoryService.getBlockById(blockId);
        validateHostelAccess(currentUser, block.getHostelId());

        BlockResponse updated = inventoryService.updateBlock(blockId, request);
        return ResponseEntity.ok(ApiResponse.ok("Block updated successfully", updated));
    }

    @DeleteMapping("/blocks/{blockId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSTEL_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBlock(
            @PathVariable Long blockId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        BlockResponse block = inventoryService.getBlockById(blockId);
        validateHostelAccess(currentUser, block.getHostelId());

        inventoryService.deleteBlock(blockId);
        return ResponseEntity.ok(ApiResponse.ok("Block deleted successfully", null));
    }

    // ==========================================
    // ROOM ENDPOINTS
    // ==========================================

    @PostMapping("/blocks/{blockId}/rooms")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSTEL_ADMIN', 'WARDEN')")
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(
            @PathVariable Long blockId,
            @Valid @RequestBody CreateRoomRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        BlockResponse block = inventoryService.getBlockById(blockId);
        validateHostelAccess(currentUser, block.getHostelId());

        RoomResponse response = inventoryService.createRoom(blockId, request);
        return new ResponseEntity<>(ApiResponse.ok("Room created successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/blocks/{blockId}/rooms")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSTEL_ADMIN', 'WARDEN', 'STAFF', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getRoomsByBlock(
            @PathVariable Long blockId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        BlockResponse block = inventoryService.getBlockById(blockId);
        validateHostelAccess(currentUser, block.getHostelId());

        List<RoomResponse> rooms = inventoryService.getRoomsByBlock(blockId);
        return ResponseEntity.ok(ApiResponse.ok(rooms));
    }

    @GetMapping("/hostels/{hostelId}/rooms")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSTEL_ADMIN', 'WARDEN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getRoomsByHostel(
            @PathVariable Long hostelId,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) String roomType,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        validateHostelAccess(currentUser, hostelId);

        List<RoomResponse> rooms = inventoryService.getRoomsByHostel(hostelId, floor, roomType);
        return ResponseEntity.ok(ApiResponse.ok(rooms));
    }

    @GetMapping("/rooms/{roomId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSTEL_ADMIN', 'WARDEN', 'STAFF', 'STUDENT')")
    public ResponseEntity<ApiResponse<RoomDetailResponse>> getRoomById(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        RoomDetailResponse room = inventoryService.getRoomById(roomId);
        validateHostelAccess(currentUser, room.getHostelId());
        return ResponseEntity.ok(ApiResponse.ok(room));
    }

    @PutMapping("/rooms/{roomId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSTEL_ADMIN', 'WARDEN')")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoom(
            @PathVariable Long roomId,
            @Valid @RequestBody UpdateRoomRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        RoomDetailResponse room = inventoryService.getRoomById(roomId);
        validateHostelAccess(currentUser, room.getHostelId());

        RoomResponse updated = inventoryService.updateRoom(roomId, request);
        return ResponseEntity.ok(ApiResponse.ok("Room updated successfully", updated));
    }

    @DeleteMapping("/rooms/{roomId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSTEL_ADMIN', 'WARDEN')")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        RoomDetailResponse room = inventoryService.getRoomById(roomId);
        validateHostelAccess(currentUser, room.getHostelId());

        inventoryService.deleteRoom(roomId);
        return ResponseEntity.ok(ApiResponse.ok("Room deleted successfully", null));
    }

    // ==========================================
    // BED ENDPOINTS
    // ==========================================

    @GetMapping("/rooms/{roomId}/beds")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSTEL_ADMIN', 'WARDEN', 'STAFF', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<BedResponse>>> getBedsByRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        RoomDetailResponse room = inventoryService.getRoomById(roomId);
        validateHostelAccess(currentUser, room.getHostelId());

        List<BedResponse> beds = inventoryService.getBedsByRoom(roomId);
        return ResponseEntity.ok(ApiResponse.ok(beds));
    }

    @GetMapping("/hostels/{hostelId}/beds/available")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSTEL_ADMIN', 'WARDEN')")
    public ResponseEntity<ApiResponse<List<BedResponse>>> getAvailableBedsByHostel(
            @PathVariable Long hostelId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        validateHostelAccess(currentUser, hostelId);

        List<BedResponse> availableBeds = inventoryService.getAvailableBedsByHostel(hostelId);
        return ResponseEntity.ok(ApiResponse.ok(availableBeds));
    }

    @PatchMapping("/beds/{bedId}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSTEL_ADMIN', 'WARDEN')")
    public ResponseEntity<ApiResponse<BedResponse>> updateBedStatus(
            @PathVariable Long bedId,
            @Valid @RequestBody UpdateBedStatusRequest request) {
        BedResponse updated = inventoryService.updateBedStatus(bedId, request);
        return ResponseEntity.ok(ApiResponse.ok("Bed status updated successfully", updated));
    }

    private void validateHostelAccess(UserPrincipal currentUser, Long targetHostelId) {
        boolean isSuperAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (!isSuperAdmin) {
            if (currentUser.getHostelId() == null || !currentUser.getHostelId().equals(targetHostelId)) {
                throw new com.hms.common.exception.UnauthorizedException(
                        "Access denied: You are not authorized to view or modify inventory in this hostel"
                );
            }
        }
    }
}
