package com.housemanager.controller;

import com.housemanager.config.ActiveUserListener;
import com.housemanager.dto.CompanyRegistrationDto;
import com.housemanager.model.Company;
import com.housemanager.model.User;
import com.housemanager.repository.BuildingRepository;
import com.housemanager.repository.CompanyRepository;
import com.housemanager.repository.LoginLogRepository;
import com.housemanager.repository.UserRepository;
import com.housemanager.service.CompanyService;
import com.housemanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    @Autowired private UserService userService;
    @Autowired private CompanyRepository companyRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private BuildingRepository buildingRepository;
    @Autowired private LoginLogRepository loginLogRepository;

    @GetMapping
    public String adminDashboard(Model model,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam(value = "keyword", required = false) String keyword) { // 1. Параметър за търсене

        if (userDetails != null) {
            model.addAttribute("username", userDetails.getUsername());
        }

        List<User> allUsers = userRepository.findAll();

        List<User> usersForTable;

        if (keyword != null && !keyword.isEmpty()) {
            usersForTable = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword);
        } else {
            usersForTable = allUsers;
        }

        model.addAttribute("users", usersForTable);
        model.addAttribute("keyword", keyword);

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
    public String listCompanies(@RequestParam(value = "sortBy", required = false) String sortBy, Model model) {
        List<Company> companies;

        if ("revenue".equals(sortBy)) {
            companies = companyRepository.findAllSortedByRevenue();
        } else {
            companies = companyRepository.findAll();
        }

        model.addAttribute("companies", companies);
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

    @PostMapping("/companies/delete/{id}")
    public String deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return "redirect:/admin/companies";
    }

    @PostMapping("/companies/update/{id}")
    public String updateCompany(@PathVariable("id") Long id,
                                @ModelAttribute CompanyRegistrationDto dto,
                                @RequestParam(value = "userId", required = false) Long userId,
                                RedirectAttributes redirectAttributes) {

        companyService.updateCompany(id, dto, userId);
        return "redirect:/admin/companies";
    }

    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable("id") Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        return "admin/user-edit";
    }

    @PostMapping("/users/update/{id}")
    public String updateUser(@PathVariable("id") Long id,
                             @ModelAttribute("user") User user,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.updateUser(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Потребителят е обновен успешно!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Грешка при обновяване: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Потребителят е изтрит успешно!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Грешка при изтриване: " + e.getMessage());
        }
        return "redirect:/admin";
    }
}