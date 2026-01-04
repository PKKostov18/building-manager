package com.housemanager.repository;

import com.housemanager.model.Building;
import com.housemanager.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {

    List<Building> findAllByCompany(Company company);
    boolean existsByAddressAndCompany(String address, Company company);
    long countByCompany(Company company);
}