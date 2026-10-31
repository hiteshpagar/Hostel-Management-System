package com.hms.modules.inventory.service;

import com.hms.common.enums.BedStatus;
import com.hms.common.exception.BadRequestException;
import com.hms.common.exception.ResourceNotFoundException;
import com.hms.entity.Bed;
import com.hms.entity.Hostel;
import com.hms.entity.HostelBlock;
import com.hms.entity.Room;
import com.hms.modules.inventory.dto.*;
import com.hms.repository.BedRepository;
import com.hms.repository.HostelBlockRepository;
import com.hms.repository.HostelRepository;
import com.hms.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final HostelRepository hostelRepository;
    private final HostelBlockRepository hostelBlockRepository;
    private final RoomRepository roomRepository;
    private final BedRepository bedRepository;

    public InventoryServiceImpl(HostelRepository hostelRepository,
                                HostelBlockRepository hostelBlockRepository,
                                RoomRepository roomRepository,
                                BedRepository bedRepository) {
        this.hostelRepository = hostelRepository;
        this.hostelBlockRepository = hostelBlockRepository;
        this.roomRepository = roomRepository;
        this.bedRepository = bedRepository;
    }

    // ==========================================
    // BLOCKS
    // ==========================================

    @Override
    @Transactional
    public BlockResponse createBlock(Long hostelId, CreateBlockRequest request) {
        Hostel hostel = hostelRepository.findById(hostelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel", "id", hostelId));

        String blockName = request.getName().trim();
        if (hostelBlockRepository.findByHostelIdAndName(hostelId, blockName).isPresent()) {
            throw new BadRequestException("A block with name '" + blockName + "' already exists in this hostel");
        }

        HostelBlock block = new HostelBlock();
        block.setHostel(hostel);
        block.setName(blockName);
        block.setGenderType(request.getGenderType());
        block.setTotalFloors(request.getTotalFloors());

        HostelBlock saved = hostelBlockRepository.save(block);
        return mapToBlockResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlockResponse> getBlocksByHostel(Long hostelId) {
        if (!hostelRepository.existsById(hostelId)) {
            throw new ResourceNotFoundException("Hostel", "id", hostelId);
        }

        return hostelBlockRepository.findByHostelId(hostelId).stream()
                .map(this::mapToBlockResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BlockResponse getBlockById(Long blockId) {
        HostelBlock block = hostelBlockRepository.findById(blockId)
                .orElseThrow(() -> new ResourceNotFoundException("HostelBlock", "id", blockId));
        return mapToBlockResponse(block);
    }

    @Override
    @Transactional
    public BlockResponse updateBlock(Long blockId, UpdateBlockRequest request) {
        HostelBlock block = hostelBlockRepository.findById(blockId)
                .orElseThrow(() -> new ResourceNotFoundException("HostelBlock", "id", blockId));

        String blockName = request.getName().trim();
        hostelBlockRepository.findByHostelIdAndName(block.getHostel().getId(), blockName)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(blockId)) {
                        throw new BadRequestException("Another block with name '" + blockName + "' already exists");
                    }
                });

        block.setName(blockName);
        block.setGenderType(request.getGenderType());
        block.setTotalFloors(request.getTotalFloors());

        HostelBlock updated = hostelBlockRepository.save(block);
        return mapToBlockResponse(updated);
    }

    @Override
    @Transactional
    public void deleteBlock(Long blockId) {
        HostelBlock block = hostelBlockRepository.findById(blockId)
                .orElseThrow(() -> new ResourceNotFoundException("HostelBlock", "id", blockId));

        List<Room> rooms = roomRepository.findByBlockId(blockId);
        if (!rooms.isEmpty()) {
            throw new BadRequestException("Cannot delete block: Contains " + rooms.size() + " room(s). Delete rooms first.");
        }

        hostelBlockRepository.delete(block);
    }

    // ==========================================
    // ROOMS
    // ==========================================

    @Override
    @Transactional
    public RoomResponse createRoom(Long blockId, CreateRoomRequest request) {
        HostelBlock block = hostelBlockRepository.findById(blockId)
                .orElseThrow(() -> new ResourceNotFoundException("HostelBlock", "id", blockId));

        String roomNum = request.getRoomNumber().trim();
        if (roomRepository.existsByBlockIdAndRoomNumber(blockId, roomNum)) {
            throw new BadRequestException("Room number '" + roomNum + "' already exists in this block");
        }

        Room room = new Room();
        room.setHostel(block.getHostel());
        room.setBlock(block);
        room.setRoomNumber(roomNum);
        room.setFloor(request.getFloor());
        room.setCapacity(request.getCapacity());
        room.setRoomType(request.getRoomType().trim().toUpperCase());
        room.setMonthlyRent(request.getMonthlyRent());

        Room savedRoom = roomRepository.save(room);

        // Automated Bed generation based on capacity (Bed A, Bed B, etc.)
        if (request.isAutoGenerateBeds()) {
            List<Bed> beds = new ArrayList<>();
            for (int i = 0; i < request.getCapacity(); i++) {
                char letter = (char) ('A' + i);
                Bed bed = new Bed();
                bed.setHostel(block.getHostel());
                bed.setRoom(savedRoom);
                bed.setBedLabel("Bed " + letter);
                bed.setStatus(BedStatus.AVAILABLE);
                beds.add(bed);
            }
            bedRepository.saveAll(beds);
        }

        return mapToRoomResponse(savedRoom);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getRoomsByBlock(Long blockId) {
        if (!hostelBlockRepository.existsById(blockId)) {
            throw new ResourceNotFoundException("HostelBlock", "id", blockId);
        }

        return roomRepository.findByBlockId(blockId).stream()
                .map(this::mapToRoomResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getRoomsByHostel(Long hostelId, Integer floor, String roomType) {
        if (!hostelRepository.existsById(hostelId)) {
            throw new ResourceNotFoundException("Hostel", "id", hostelId);
        }

        List<Room> rooms = roomRepository.findByHostelId(hostelId);

        return rooms.stream()
                .filter(r -> floor == null || r.getFloor().equals(floor))
                .filter(r -> !StringUtils.hasText(roomType) || r.getRoomType().equalsIgnoreCase(roomType.trim()))
                .map(this::mapToRoomResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoomDetailResponse getRoomById(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", roomId));

        RoomDetailResponse response = new RoomDetailResponse();
        populateRoomFields(response, room);

        List<BedResponse> bedResponses = bedRepository.findByRoomId(roomId).stream()
                .map(this::mapToBedResponse)
                .collect(Collectors.toList());

        response.setBeds(bedResponses);
        return response;
    }

    @Override
    @Transactional
    public RoomResponse updateRoom(Long roomId, UpdateRoomRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", roomId));

        String roomNum = request.getRoomNumber().trim();
        roomRepository.findByBlockIdAndRoomNumber(room.getBlock().getId(), roomNum)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(roomId)) {
                        throw new BadRequestException("Room number '" + roomNum + "' already exists in this block");
                    }
                });

        room.setRoomNumber(roomNum);
        room.setFloor(request.getFloor());
        room.setRoomType(request.getRoomType().trim().toUpperCase());
        room.setMonthlyRent(request.getMonthlyRent());

        Room updated = roomRepository.save(room);
        return mapToRoomResponse(updated);
    }

    @Override
    @Transactional
    public void deleteRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", roomId));

        List<Bed> beds = bedRepository.findByRoomId(roomId);
        boolean hasOccupiedBed = beds.stream().anyMatch(b -> b.getStatus() == BedStatus.OCCUPIED);
        if (hasOccupiedBed) {
            throw new BadRequestException("Cannot delete room: Contains one or more occupied bed(s).");
        }

        bedRepository.deleteAll(beds);
        roomRepository.delete(room);
    }

    // ==========================================
    // BEDS
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<BedResponse> getBedsByRoom(Long roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new ResourceNotFoundException("Room", "id", roomId);
        }

        return bedRepository.findByRoomId(roomId).stream()
                .map(this::mapToBedResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BedResponse> getAvailableBedsByHostel(Long hostelId) {
        if (!hostelRepository.existsById(hostelId)) {
            throw new ResourceNotFoundException("Hostel", "id", hostelId);
        }

        return bedRepository.findByHostelIdAndStatus(hostelId, BedStatus.AVAILABLE).stream()
                .map(this::mapToBedResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BedResponse updateBedStatus(Long bedId, UpdateBedStatusRequest request) {
        Bed bed = bedRepository.findById(bedId)
                .orElseThrow(() -> new ResourceNotFoundException("Bed", "id", bedId));

        if (bed.getStatus() == BedStatus.OCCUPIED && request.getStatus() != BedStatus.OCCUPIED) {
            throw new BadRequestException("Cannot manually change status of an OCCUPIED bed. Vacate the resident first.");
        }

        bed.setStatus(request.getStatus());
        Bed updated = bedRepository.save(bed);
        return mapToBedResponse(updated);
    }

    // ==========================================
    // MAPPERS
    // ==========================================

    private BlockResponse mapToBlockResponse(HostelBlock block) {
        int roomCount = roomRepository.findByBlockId(block.getId()).size();
        return new BlockResponse(
                block.getId(),
                block.getHostel().getId(),
                block.getHostel().getName(),
                block.getName(),
                block.getGenderType(),
                block.getTotalFloors(),
                roomCount,
                block.getCreatedAt()
        );
    }

    private RoomResponse mapToRoomResponse(Room room) {
        RoomResponse response = new RoomResponse();
        populateRoomFields(response, room);
        return response;
    }

    private void populateRoomFields(RoomResponse response, Room room) {
        response.setId(room.getId());
        response.setHostelId(room.getHostel().getId());
        response.setBlockId(room.getBlock().getId());
        response.setBlockName(room.getBlock().getName());
        response.setRoomNumber(room.getRoomNumber());
        response.setFloor(room.getFloor());
        response.setCapacity(room.getCapacity());
        response.setRoomType(room.getRoomType());
        response.setMonthlyRent(room.getMonthlyRent());
        response.setCreatedAt(room.getCreatedAt());

        List<Bed> beds = bedRepository.findByRoomId(room.getId());
        int occupied = (int) beds.stream().filter(b -> b.getStatus() == BedStatus.OCCUPIED).count();
        int available = (int) beds.stream().filter(b -> b.getStatus() == BedStatus.AVAILABLE).count();

        response.setOccupiedBeds(occupied);
        response.setAvailableBeds(available);
    }

    private BedResponse mapToBedResponse(Bed bed) {
        return new BedResponse(
                bed.getId(),
                bed.getHostel().getId(),
                bed.getRoom().getId(),
                bed.getRoom().getRoomNumber(),
                bed.getBedLabel(),
                bed.getStatus(),
                bed.getCreatedAt()
        );
    }
}
