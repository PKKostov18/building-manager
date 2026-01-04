package com.housemanager.dto;

import lombok.Data;

@Data
public class EmployeeRegistrationDto {
    private String name;
    private String phoneNumber;
    private String email;
    private String username;
    private String password;
}