package com.hms.modules.inventory.dto;

import java.util.ArrayList;
import java.util.List;

public class RoomDetailResponse extends RoomResponse {

    private List<BedResponse> beds = new ArrayList<>();

    public RoomDetailResponse() {
    }

    public List<BedResponse> getBeds() {
        return beds;
    }

    public void setBeds(List<BedResponse> beds) {
        this.beds = beds;
    }
}
