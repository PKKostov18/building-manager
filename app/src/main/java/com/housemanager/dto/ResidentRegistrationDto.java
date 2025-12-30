package com.housemanager.dto;

import lombok.Data;

@Data
public class ResidentRegistrationDto {
    private String firstName;
    private String lastName;
    private int age;
    private boolean usesElevator;
    private boolean createAccount;
    private String username;
    private String password;
    private String email;
}