package com.housemanager.service;

import com.housemanager.dto.ResidentRegistrationDto;
import com.housemanager.model.Apartment;
import com.housemanager.model.Resident;
import com.housemanager.model.RoleType;
import com.housemanager.model.User;
import com.housemanager.repository.ApartmentRepository;
import com.housemanager.repository.ResidentRepository;
import com.housemanager.repository.RoleRepository;
import com.housemanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class ResidentService {

    @Autowired private ResidentRepository residentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ApartmentRepository apartmentRepository;

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

    @Transactional
    public void updateResidentDetails(Long id, String fullName, Integer age, Boolean usesElevator) {
        Resident resident = residentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resident not found"));

        String[] names = fullName.split(" ", 2);
        resident.setFirstName(names[0]);
        if (names.length > 1) {
            resident.setLastName(names[1]);
        }

        resident.setAge(age);
        resident.setUsesElevator(usesElevator);

        residentRepository.save(resident);
    }

    @Transactional
    public void deleteResident(Long id) {
        Resident resident = residentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resident not found"));

        User userToDelete = resident.getUser();

        if (resident.getApartment() != null) {
            resident.setApartment(null);
        }

        if (userToDelete != null) {
            List<Apartment> ownedApartments = apartmentRepository.findByOwner(userToDelete);

            for (Apartment apt : ownedApartments) {
                apt.setOwner(null);
                apartmentRepository.save(apt);
            }

            resident.setUser(null);
        }

        residentRepository.delete(resident);

        if (userToDelete != null) {
            userRepository.delete(userToDelete);
        }
    }
}