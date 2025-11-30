package com.housemanager.service;

import com.housemanager.model.Building;
import com.housemanager.model.Company;
import com.housemanager.model.Employee;
import com.housemanager.repository.BuildingRepository;
import com.housemanager.repository.CompanyRepository;
import com.housemanager.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class BuildingService {

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    public List<Building> findMyBuildings(String username) {
        Company company = companyRepository.findByUser_Username(username)
                .orElseThrow(() -> new RuntimeException("Company profile not found for user: " + username));

        return buildingRepository.findAllByCompany(company);
    }

    @Transactional
    public void createBuilding(Building building, String username) {
        Company company = companyRepository.findByUser_Username(username)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        building.setCompany(company);

        List<Employee> employees = employeeRepository.findAllByCompany(company);

        if (!employees.isEmpty()) {
            Employee bestCandidate = employees.stream()
                    .min(Comparator.comparingInt(e -> e.getBuildings().size()))
                    .orElse(employees.get(0));

            building.setEmployee(bestCandidate);
        }

        buildingRepository.save(building);
    }
}