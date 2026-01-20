package com.housemanager.controller;

import com.housemanager.dto.EmployeeRegistrationDto;
import com.housemanager.model.Building;
import com.housemanager.model.Company;
import com.housemanager.model.Employee;
import com.housemanager.model.User;
import com.housemanager.repository.BuildingRepository;
import com.housemanager.repository.CompanyRepository;
import com.housemanager.repository.EmployeeRepository;
import com.housemanager.repository.UserRepository;
import com.housemanager.service.BuildingService;
import com.housemanager.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;

@Controller
@RequestMapping("/company")
public class CompanyController {

    @Autowired private BuildingService buildingService;
    @Autowired private EmployeeService employeeService;
    @Autowired private UserRepository userRepository;
    @Autowired private CompanyRepository companyRepository;
    @Autowired private BuildingRepository buildingRepository;
    @Autowired private EmployeeRepository employeeRepository;

    @GetMapping
    public String showCompanyDashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Company company = companyRepository.findByUser(user);

        if (company != null) {
            long totalBuildings = buildingRepository.countByCompany(company);
            long totalEmployees = employeeRepository.countByCompany(company);

            model.addAttribute("company", company);
            model.addAttribute("totalBuildings", totalBuildings);
            model.addAttribute("totalEmployees", totalEmployees);
        }

        return "company/index";
    }

    @GetMapping("/buildings")
    public String showMyBuildings(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Company company = companyRepository.findByUser(user);

        if (company != null) {
            List<Building> buildings = buildingService.findMyBuildings(userDetails.getUsername());
            List<Employee> employees = employeeRepository.findByCompany(company);

            model.addAttribute("buildings", buildings);
            model.addAttribute("employees", employees);
        }

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

    @PostMapping("/buildings/update/{id}")
    public String updateBuilding(@PathVariable("id") Long id,
                                 @ModelAttribute Building building,
                                 @RequestParam(value = "employeeId", required = false) Long employeeId) {

        buildingService.updateBuilding(id, building, employeeId);
        return "redirect:/company/buildings";
    }

    @PostMapping("/buildings/delete/{id}")
    public String deleteBuilding(@PathVariable("id") Long id) {
        buildingService.deleteBuilding(id);
        return "redirect:/company/buildings";
    }

    @GetMapping("/employees")
    public String showEmployees(@RequestParam(value = "sortBy", required = false) String sortBy,
                                @RequestParam(value = "keyword", required = false) String keyword,
                                Model model,
                                @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Company currentCompany = companyRepository.findByUser(user);

        List<Employee> employees;

        if (currentCompany != null) {
            if (keyword != null && !keyword.isEmpty()) {
                employees = employeeRepository.findByCompanyAndNameContainingIgnoreCase(currentCompany, keyword);
            } else if ("buildings".equals(sortBy)) {
                employees = employeeRepository.findByCompanyOrderByBuildingsCount(currentCompany);
            } else if ("name".equals(sortBy)) {
                employees = employeeRepository.findByCompanyOrderByNameAsc(currentCompany);
            } else {
                employees = employeeRepository.findByCompany(currentCompany);
            }
        } else {
            employees = new ArrayList<>();
        }

        model.addAttribute("employees", employees);
        model.addAttribute("keyword", keyword);

        return "company/employees-list";
    }

    @GetMapping("/employees/new")
    public String showCreateEmployeeForm(Model model) {
        model.addAttribute("employeeForm", new EmployeeRegistrationDto());
        return "company/create-employee";
    }

    @PostMapping("/employees/save")
    public String saveEmployee(@ModelAttribute("employeeForm") EmployeeRegistrationDto dto,
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

    @PostMapping("/employees/update/{id}")
    public String updateEmployee(@PathVariable("id") Long id,
                                 @ModelAttribute("employeeForm") EmployeeRegistrationDto dto) {
        employeeService.updateEmployee(id, dto);
        return "redirect:/company/employees";
    }

    @PostMapping("/employees/delete/{id}")
    public String deleteEmployee(@PathVariable("id") Long id) {
        employeeService.deleteEmployee(id);
        return "redirect:/company/employees";
    }
}