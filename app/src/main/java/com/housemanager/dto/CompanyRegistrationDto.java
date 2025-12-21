package com.housemanager.dto;

import lombok.Data;

@Data
public class CompanyRegistrationDto {

    private String companyName;
    private String bulstat;
    private String address;
    private String contactPerson;

    private Double defaultTaxPerSqM;
    private Double defaultElevatorTax;
    private Double defaultPetTax;

    private String username;
    private String email;
    private String password;
}