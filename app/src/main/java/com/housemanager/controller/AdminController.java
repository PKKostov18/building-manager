package com.housemanager.controller;

import com.housemanager.dto.CompanyRegistrationDto;
import com.housemanager.model.User;
import com.housemanager.repository.BuildingRepository; // <--- НОВО
import com.housemanager.repository.CompanyRepository;
import com.housemanager.repository.UserRepository;
import com.housemanager.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CompanyService companyService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BuildingRepository buildingRepository;

    @GetMapping
    public String showAdminIndex(Model model) {
        List<User> allUsers = userRepository.findAll();
        model.addAttribute("users", allUsers);

        long totalCompanies = companyRepository.count();
        long totalBuildings = buildingRepository.count();
        long totalUsers = allUsers.size();

        long totalEmployees = allUsers.stream()
                .filter(u -> u.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE")))
                .count();

        long totalResidents = allUsers.stream()
                .filter(u -> u.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_RESIDENT")))
                .count();

        model.addAttribute("totalCompanies", totalCompanies);
        model.addAttribute("totalBuildings", totalBuildings);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalEmployees", totalEmployees);
        model.addAttribute("totalResidents", totalResidents);

        return "admin/index";
    }

    @GetMapping("/companies")
    public String showCompanyList(Model model) {
        model.addAttribute("companies", companyRepository.findAll());
        return "admin/companies-list";
    }

    @GetMapping("/companies/new")
    public String showCreateCompanyForm(Model model) {
        model.addAttribute("companyForm", new CompanyRegistrationDto());
        return "admin/create-company";
    }

    @PostMapping("/companies/save")
    public String saveCompany(@ModelAttribute("companyForm") CompanyRegistrationDto dto, Model model) {
        try {
            companyService.createCompany(dto);
        } catch (RuntimeException ex) {
            model.addAttribute("companyForm", dto);
            model.addAttribute("error", ex.getMessage());
            return "admin/create-company";
        }
        return "redirect:/admin/companies";
    }
}