package com.housemanager.repository;

import com.housemanager.model.Company;
import com.housemanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Boolean existsByName(String name);
    Boolean existsByBulstat(String bulstat);
    Optional<Company> findByUser_Username(String username);
    Company findByUser(User user);
}