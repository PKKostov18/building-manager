package com.housemanager.controller;

import com.housemanager.dto.CompanyRegistrationDto;
import com.housemanager.repository.CompanyRepository;
import com.housemanager.repository.UserRepository; // Импортни това
import com.housemanager.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CompanyService companyService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;
    @GetMapping
    public String showAdminIndex(Model model) {
        long totalCompanies = companyRepository.count();
        long totalUsers = userRepository.count();

        model.addAttribute("totalCompanies", totalCompanies);
        model.addAttribute("totalUsers", totalUsers);

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