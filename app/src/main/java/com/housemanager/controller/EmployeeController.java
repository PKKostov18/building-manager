package com.housemanager.controller;

import com.housemanager.dto.ApartmentConfigDto;
import com.housemanager.dto.ApartmentViewDto;
import com.housemanager.model.Apartment;
import com.housemanager.model.Building; // Добавен импорт
import com.housemanager.model.Employee;
import com.housemanager.model.Resident;
import com.housemanager.model.User;
import com.housemanager.repository.ApartmentRepository;
import com.housemanager.repository.ResidentRepository;
import com.housemanager.repository.UserRepository;
import com.housemanager.service.EmployeeService;
import com.housemanager.service.PaymentService;
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
    @Autowired private PaymentService paymentService;

    // --- ОБНОВЕН МЕТОД ЗА DASHBOARD ---
    @GetMapping
    public String showEmployeeDashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // 1. Намираме текущия служител
        Employee employee = employeeService.findCurrentEmployee(userDetails.getUsername());
        List<Building> myBuildings = employee.getBuildings();

        // 2. Инициализираме променливи за статистика
        int totalApartments = 0;
        int totalResidents = 0;
        double totalCollected = 0.0;
        double totalExpected = 0.0;

        // 3. Завъртаме цикъл през всички сгради на служителя
        for (Building b : myBuildings) {
            // Взимаме апартаментите от базата, за да е сигурно зареждането
            List<Apartment> apartments = apartmentRepository.findAllByBuilding(b);
            totalApartments += apartments.size();

            for (Apartment apt : apartments) {
                // Броим жителите
                if (apt.getResidents() != null) {
                    totalResidents += apt.getResidents().size();
                }

                // Изчисляваме финансите
                double fee = taxService.calculateMonthlyFee(apt);

                // Смятаме, че апартаментът е "активен", ако има собственик или жители
                boolean isOccupied = (apt.getOwner() != null || (apt.getResidents() != null && !apt.getResidents().isEmpty()));

                if (isOccupied && fee > 0) {
                    totalExpected += fee; // Това трябва да се събере

                    // Проверяваме дали е платено
                    if (paymentService.isPaidForCurrentMonth(apt)) {
                        totalCollected += fee;
                    }
                }
            }
        }

        // Изчисляваме % събраемост (защита от делене на 0)
        double collectionRate = (totalExpected > 0) ? (totalCollected / totalExpected) * 100 : 0;

        // 4. Подаваме данните към HTML
        model.addAttribute("employee", employee);
        model.addAttribute("buildingsCount", myBuildings.size());

        // Нови данни за разширеното табло
        model.addAttribute("totalApartments", totalApartments);
        model.addAttribute("totalResidents", totalResidents);
        model.addAttribute("totalCollected", totalCollected);
        model.addAttribute("totalExpected", totalExpected);
        model.addAttribute("collectionRate", (int) collectionRate);

        return "employee/index";
    }

    // --- ОСТАНАЛИТЕ МЕТОДИ СИ ОСТАВАТ СЪЩИТЕ ---

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

        List<ApartmentViewDto> apartmentDtos = new java.util.ArrayList<>();

        for (var apt : apartments) {
            double fee = taxService.calculateMonthlyFee(apt);

            boolean hasDebt = false;
            boolean isOccupied = (apt.getOwner() != null || !apt.getResidents().isEmpty());

            if (isOccupied && fee > 0) {
                boolean isPaid = paymentService.isPaidForCurrentMonth(apt);

                if (isPaid) {
                    totalCollected += fee;
                } else {
                    totalDebts += fee;
                    hasDebt = true;
                    apartmentsWithDebtCount++;
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