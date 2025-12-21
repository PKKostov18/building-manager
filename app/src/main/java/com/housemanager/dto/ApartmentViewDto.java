package com.housemanager.dto;

import com.housemanager.model.Apartment;
import lombok.Data;

@Data
public class ApartmentViewDto {
    private Apartment apartment;
    private double monthlyFee;
    private boolean hasDebt;

    public ApartmentViewDto(Apartment apartment, double monthlyFee, boolean hasDebt) {
        this.apartment = apartment;
        this.monthlyFee = monthlyFee;
        this.hasDebt = hasDebt;
    }
}