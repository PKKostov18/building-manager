package com.housemanager.controller;

import com.housemanager.model.Building;
import com.housemanager.service.BuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/company")
public class CompanyController {

    @Autowired
    private BuildingService buildingService;

    @Autowired
    private com.housemanager.service.EmployeeService employeeService;

    @GetMapping
    public String showCompanyDashboard() {
        return "company/index";
    }

    @GetMapping("/buildings")
    public String showMyBuildings(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("buildings", buildingService.findMyBuildings(userDetails.getUsername()));
        return "company/buildings-list";
    }

    @GetMapping("/buildings/new")
    public String showCreateBuildingForm(Model model) {
        model.addAttribute("building", new Building());
        return "company/create-building";
    }

    @PostMapping("/buildings/save")
    public String saveBuilding(@ModelAttribute("building") Building building,
                               @AuthenticationPrincipal UserDetails userDetails) {

        buildingService.createBuilding(building, userDetails.getUsername());
        return "redirect:/company/buildings";
    }

    // --- Employee Management ---

    @GetMapping("/employees")
    public String showEmployees(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("employees", employeeService.findEmployeesByCompanyUser(userDetails.getUsername()));
        return "company/employees-list";
    }

    @GetMapping("/employees/new")
    public String showCreateEmployeeForm(Model model) {
        model.addAttribute("employeeForm", new com.housemanager.dto.EmployeeRegistrationDto());
        return "company/create-employee";
    }

    @PostMapping("/employees/save")
    public String saveEmployee(@ModelAttribute("employeeForm") com.housemanager.dto.EmployeeRegistrationDto dto,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        try {
            employeeService.createEmployee(dto, userDetails.getUsername());
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("employeeForm", dto);
            return "company/create-employee";
        }

        return "redirect:/company/employees";
    }
}