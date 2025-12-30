package com.housemanager.service;

import com.housemanager.dto.ResidentRegistrationDto;
import com.housemanager.model.Resident;
import com.housemanager.model.RoleType;
import com.housemanager.model.User;
import com.housemanager.repository.ResidentRepository;
import com.housemanager.repository.RoleRepository;
import com.housemanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class ResidentService {

    @Autowired private ResidentRepository residentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Transactional
    public void createResident(ResidentRegistrationDto dto) {

        Resident resident = new Resident();
        resident.setFirstName(dto.getFirstName());
        resident.setLastName(dto.getLastName());
        resident.setAge(dto.getAge());
        resident.setUsesElevator(dto.isUsesElevator());

        if (dto.isCreateAccount()) {

            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("Email address " + dto.getEmail() + " is already in use!");
            }
            if (userRepository.existsByUsername(dto.getUsername())) {
                throw new IllegalArgumentException("Username " + dto.getUsername() + " is already taken!");
            }

            User user = new User();
            user.setUsername(dto.getUsername());
            user.setEmail(dto.getEmail());
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setResident(true);

            var residentRole = roleRepository.findByName(RoleType.ROLE_RESIDENT)
                    .orElseThrow(() -> new RuntimeException("Error: Role RESIDENT is not found."));
            user.setRoles(Set.of(residentRole));

            userRepository.save(user);
            resident.setUser(user);
        } else {
            resident.setUser(null);
        }

        residentRepository.save(resident);
    }
}