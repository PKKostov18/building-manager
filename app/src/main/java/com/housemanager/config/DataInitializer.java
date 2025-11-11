package com.housemanager.config;

import com.housemanager.model.Role;
import com.housemanager.model.RoleType;
import com.housemanager.model.User;
import com.housemanager.repository.RoleRepository;
import com.housemanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${housemanager.admin.default-password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {

        Role adminRole = roleRepository.findByName(RoleType.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RoleType.ROLE_ADMIN)));

        roleRepository.findByName(RoleType.ROLE_COMPANY)
                .orElseGet(() -> roleRepository.save(new Role(RoleType.ROLE_COMPANY)));

        roleRepository.findByName(RoleType.ROLE_RESIDENT)
                .orElseGet(() -> roleRepository.save(new Role(RoleType.ROLE_RESIDENT)));


        if (!userRepository.existsByUsername("admin")) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@housemanager.com");
            adminUser.setPassword(passwordEncoder.encode(adminPassword));
            adminUser.setRoles(Set.of(adminRole));

            userRepository.save(adminUser);
        }
    }
}