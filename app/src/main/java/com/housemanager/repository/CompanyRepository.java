package com.housemanager.repository;

import com.housemanager.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Boolean existsByName(String name);
    Boolean existsByBulstat(String bulstat);
}