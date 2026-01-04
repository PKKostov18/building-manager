package com.housemanager.controller;

import com.housemanager.config.ActiveUserListener; // <--- Импорт
import com.housemanager.dto.CompanyRegistrationDto;
import com.housemanager.model.User;
import com.housemanager.repository.BuildingRepository;
import com.housemanager.repository.CompanyRepository;
import com.housemanager.repository.LoginLogRepository;
import com.housemanager.repository.UserRepository;
import com.housemanager.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private CompanyService companyService;
    @Autowired private CompanyRepository companyRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private BuildingRepository buildingRepository;
    @Autowired private LoginLogRepository loginLogRepository;

    @GetMapping
    public String adminDashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            model.addAttribute("username", userDetails.getUsername());
        }

        List<User> allUsers = userRepository.findAll();
        model.addAttribute("users", allUsers);

        long totalCompanies = companyRepository.count();
        long totalBuildings = buildingRepository.count();
        long totalEmployees = allUsers.stream().filter(u -> u.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"))).count();
        long totalResidents = allUsers.stream().filter(u -> u.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_RESIDENT"))).count();

        model.addAttribute("totalCompanies", totalCompanies);
        model.addAttribute("totalBuildings", totalBuildings);
        model.addAttribute("totalEmployees", totalEmployees);
        model.addAttribute("totalResidents", totalResidents);

        List<String> chartDates = new ArrayList<>();
        List<Long> chartActivity = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            chartDates.add(date.format(formatter));
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            chartActivity.add(loginLogRepository.countByTimestampBetween(startOfDay, endOfDay));
        }
        model.addAttribute("chartLabels", chartDates);
        model.addAttribute("chartData", chartActivity);

        int activeSessions = ActiveUserListener.getTotalActiveSession();
        if (activeSessions == 0) activeSessions = 1;
        model.addAttribute("activeSessions", activeSessions);

        long totalMem = Runtime.getRuntime().totalMemory();
        long freeMem = Runtime.getRuntime().freeMemory();
        long usedMem = totalMem - freeMem;

        long loadPercentage = (usedMem * 100) / totalMem;
        model.addAttribute("serverLoad", loadPercentage);

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