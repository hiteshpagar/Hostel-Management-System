package com.hms.modules.inventory.dto;

import com.hms.common.enums.GenderType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateBlockRequest {

    @NotBlank(message = "Block name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Gender type is required")
    private GenderType genderType;

    @NotNull(message = "Total floors count is required")
    @Min(value = 1, message = "Total floors must be at least 1")
    private Integer totalFloors;

    public UpdateBlockRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GenderType getGenderType() {
        return genderType;
    }

    public void setGenderType(GenderType genderType) {
        this.genderType = genderType;
    }

    public Integer getTotalFloors() {
        return totalFloors;
    }

    public void setTotalFloors(Integer totalFloors) {
        this.totalFloors = totalFloors;
    }
}
