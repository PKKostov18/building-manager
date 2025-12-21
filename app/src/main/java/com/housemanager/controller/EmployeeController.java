package com.housemanager.controller;

import com.housemanager.model.Employee;
import com.housemanager.repository.ApartmentRepository;
import com.housemanager.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private ApartmentRepository apartmentRepository; // Добавете това

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

        boolean hasAccess = employee.getBuildings().stream()
                .anyMatch(b -> b.getId().equals(id));

        if (!hasAccess) {
            return "redirect:/employee/buildings";
        }

        var building = employee.getBuildings().stream()
                .filter(b -> b.getId().equals(id))
                .findFirst()
                .orElseThrow();

        var apartments = apartmentRepository.findAllByBuilding(building);
        apartments.sort(java.util.Comparator.comparing(com.housemanager.model.Apartment::getNumber));

        var apartmentsByFloor = apartments.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        com.housemanager.model.Apartment::getFloor,
                        java.util.TreeMap::new,
                        java.util.stream.Collectors.toList()
                ));

        model.addAttribute("building", building);
        model.addAttribute("apartmentsByFloor", apartmentsByFloor);
        model.addAttribute("totalApartments", apartments.size());

        model.addAttribute("apartmentsWithDebt", 5);
        model.addAttribute("cashBalance", 12050.00);
        model.addAttribute("totalLiabilities", 3400.00);

        return "employee/building-details";
    }
}