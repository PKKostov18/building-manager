package com.housemanager.controller;

import com.housemanager.dto.ApartmentConfigDto;
import com.housemanager.model.Apartment;
import com.housemanager.model.Employee;
import com.housemanager.model.Resident;
import com.housemanager.model.User;
import com.housemanager.repository.ApartmentRepository;
import com.housemanager.repository.ResidentRepository;
import com.housemanager.repository.UserRepository;
import com.housemanager.service.EmployeeService;
import com.housemanager.service.TaxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired private EmployeeService employeeService;
    @Autowired private ApartmentRepository apartmentRepository;
    @Autowired private ResidentRepository residentRepository;
    @Autowired private TaxService taxService;
    @Autowired private UserRepository userRepository;

    @GetMapping
    public String showEmployeeDashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Employee employee = employeeService.findCurrentEmployee(userDetails.getUsername());

        model.addAttribute("employee", employee);
        model.addAttribute("buildingsCount", employee.getBuildings().size());

        return "employee/index";
    }

    @GetMapping("/buildings")
    public String showMyBuildings(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Employee employee = employeeService.findCurrentEmployee(userDetails.getUsername());

        model.addAttribute("buildings", employee.getBuildings());

        return "employee/my-buildings";
    }


    @GetMapping("/buildings/{id}")
    public String showBuildingDetails(@PathVariable Long id, Model model,
                                      @AuthenticationPrincipal UserDetails userDetails) {

        Employee employee = employeeService.findCurrentEmployee(userDetails.getUsername());
        boolean hasAccess = employee.getBuildings().stream().anyMatch(b -> b.getId().equals(id));
        if (!hasAccess) return "redirect:/employee/buildings";

        var building = employee.getBuildings().stream()
                .filter(b -> b.getId().equals(id)).findFirst().orElseThrow();

        var apartments = apartmentRepository.findAllByBuilding(building);

        List<User> allUsers = userRepository.findAvailableOwners();
        model.addAttribute("allUsers", allUsers);

        List<Resident> allResidents = residentRepository.findByUserIsNullAndApartmentIsNull();
        model.addAttribute("allResidents", allResidents);

        double totalCollected = 0.0;
        double totalDebts = 0.0;
        int apartmentsWithDebtCount = 0;

        java.util.List<com.housemanager.dto.ApartmentViewDto> apartmentDtos = new java.util.ArrayList<>();

        for (var apt : apartments) {
            double fee = taxService.calculateMonthlyFee(apt);

            boolean hasDebt = false;
            if (apt.getOwner() != null) {
                hasDebt = apt.getNumber().contains("1");
                if (hasDebt) {
                    totalDebts += fee;
                    apartmentsWithDebtCount++;
                } else {
                    totalCollected += fee;
                }
            }

            apartmentDtos.add(new com.housemanager.dto.ApartmentViewDto(apt, fee, hasDebt));
        }

        var apartmentsByFloor = apartmentDtos.stream()
                .sorted(java.util.Comparator.comparingInt(dto -> {
                    String numStr = dto.getApartment().getNumber();
                    String digitsOnly = numStr.replaceAll("[^0-9]", "");
                    if (digitsOnly.isEmpty()) return 999999;
                    return Integer.parseInt(digitsOnly);
                }))
                .collect(java.util.stream.Collectors.groupingBy(
                        dto -> dto.getApartment().getFloor(),
                        java.util.TreeMap::new,
                        java.util.stream.Collectors.toList()
                ));

        model.addAttribute("building", building);
        model.addAttribute("apartmentsByFloor", apartmentsByFloor);
        model.addAttribute("totalApartments", apartments.size());
        model.addAttribute("apartmentsWithDebt", apartmentsWithDebtCount);
        model.addAttribute("cashBalance", totalCollected);
        model.addAttribute("totalLiabilities", totalDebts);

        return "employee/building-details";
    }

    @PostMapping("/buildings/configure-apartment")
    public String configureApartment(ApartmentConfigDto dto) {
        Apartment apt = apartmentRepository.findById(dto.getApartmentId()).orElseThrow();

        if (dto.getArea() != null) {
            apt.setArea(dto.getArea());
        }

        User owner = userRepository.findById(dto.getOwnerId()).orElseThrow();
        apt.setOwner(owner);
        apt.setHasPet(dto.isHasPet());

        List<Resident> currentResidents = residentRepository.findAllByApartment(apt);
        for (Resident r : currentResidents) {
            r.setApartment(null);
            residentRepository.save(r);
        }

        Resident ownerAsResident = residentRepository.findByUser(owner);
        if (ownerAsResident != null) {
            ownerAsResident.setApartment(apt);
            residentRepository.save(ownerAsResident);
        }

        if (dto.getResidentIds() != null) {
            List<Resident> newResidents = residentRepository.findAllById(dto.getResidentIds());
            for (Resident r : newResidents) {
                r.setApartment(apt);
                residentRepository.save(r);
            }
        }

        apartmentRepository.save(apt);
        return "redirect:/employee/buildings/" + apt.getBuilding().getId();
    }
}