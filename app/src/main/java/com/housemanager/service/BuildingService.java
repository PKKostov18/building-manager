package com.housemanager.service;

import com.housemanager.model.*;
import com.housemanager.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class BuildingService {

    @Autowired private BuildingRepository buildingRepository;
    @Autowired private CompanyRepository companyRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private ApartmentRepository apartmentRepository;
    @Autowired private ResidentRepository residentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PaymentRepository paymentRepository;

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

    @Transactional
    public void updateBuilding(Long id, Building updatedInfo, Long employeeId) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Building not found"));

        building.setAddress(updatedInfo.getAddress());
        building.setTotalFloors(updatedInfo.getTotalFloors());
        building.setTotalApartments(updatedInfo.getTotalApartments());
        building.setBuiltArea(updatedInfo.getBuiltArea());

        if (employeeId != null) {
            Employee emp = employeeRepository.findById(employeeId).orElse(null);
            building.setEmployee(emp);
        } else {
            building.setEmployee(null);
        }

        buildingRepository.save(building);
    }

    @Transactional
    public void deleteBuilding(Long id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Building not found"));

        List<Apartment> apartments = apartmentRepository.findByBuilding(building);
        Set<User> ownersToDelete = new HashSet<>();

        for (Apartment apartment : apartments) {
            if (apartment.getOwner() != null) {
                ownersToDelete.add(apartment.getOwner());
                apartment.setOwner(null); // Разкачаме веднага
                apartmentRepository.save(apartment);
            }

            List<Resident> residents = residentRepository.findByApartment(apartment);
            residentRepository.deleteAll(residents);

            List<Payment> aptPayments = paymentRepository.findByApartment(apartment);
            paymentRepository.deleteAll(aptPayments);
        }

        residentRepository.flush();
        paymentRepository.flush();

        apartmentRepository.deleteAll(apartments);
        apartmentRepository.flush();

        buildingRepository.delete(building);
        buildingRepository.flush();

        for (User owner : ownersToDelete) {
            List<Payment> userPayments = paymentRepository.findByPayer(owner);
            paymentRepository.deleteAll(userPayments);

            if (userRepository.existsById(owner.getId())) {
                userRepository.delete(owner);
            }
        }
        userRepository.flush();
    }
}