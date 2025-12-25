package com.housemanager.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApartmentConfigDto {
    private Long apartmentId;
    private Long ownerId;
    private boolean hasPet;
    private List<Long> residentIds;
}