package com.housemanager.service;

import com.housemanager.model.Apartment;
import com.housemanager.model.Building;
import com.housemanager.model.Company;
import com.housemanager.model.Employee;
import com.housemanager.repository.ApartmentRepository;
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

    @Autowired
    private ApartmentRepository apartmentRepository;

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

        building = buildingRepository.save(building);

        generateEmptyApartments(building);
    }

    private void generateEmptyApartments(Building building) {
        int totalApts = building.getTotalApartments();
        int totalFloors = building.getTotalFloors();

        int aptsPerFloor = (int) Math.ceil((double) totalApts / totalFloors);

        int currentFloor = 1;
        int aptsOnCurrentFloor = 0;

        for (int i = 1; i <= totalApts; i++) {
            Apartment apt = new Apartment();
            apt.setNumber("Apt " + i);
            apt.setBuilding(building);
            apt.setArea(0.00);
            apt.setHasPet(false);

            apt.setFloor(currentFloor);
            aptsOnCurrentFloor++;

            if (aptsOnCurrentFloor >= aptsPerFloor && currentFloor < totalFloors) {
                currentFloor++;
                aptsOnCurrentFloor = 0;
            }

            apartmentRepository.save(apt);
        }
    }
}