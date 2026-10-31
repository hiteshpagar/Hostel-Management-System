package com.hms.modules.inventory.service;

import com.hms.modules.inventory.dto.*;

import java.util.List;

public interface InventoryService {

    // Blocks
    BlockResponse createBlock(Long hostelId, CreateBlockRequest request);

    List<BlockResponse> getBlocksByHostel(Long hostelId);

    BlockResponse getBlockById(Long blockId);

    BlockResponse updateBlock(Long blockId, UpdateBlockRequest request);

    void deleteBlock(Long blockId);

    // Rooms
    RoomResponse createRoom(Long blockId, CreateRoomRequest request);

    List<RoomResponse> getRoomsByBlock(Long blockId);

    List<RoomResponse> getRoomsByHostel(Long hostelId, Integer floor, String roomType);

    RoomDetailResponse getRoomById(Long roomId);

    RoomResponse updateRoom(Long roomId, UpdateRoomRequest request);

    void deleteRoom(Long roomId);

    // Beds
    List<BedResponse> getBedsByRoom(Long roomId);

    List<BedResponse> getAvailableBedsByHostel(Long hostelId);

    BedResponse updateBedStatus(Long bedId, UpdateBedStatusRequest request);
}
