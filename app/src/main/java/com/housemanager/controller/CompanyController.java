package com.housemanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/company")
public class CompanyController {

    // TODO: Инжектирай EmployeeRepository и BuildingRepository, за да показваш статистики

    @GetMapping
    public String showCompanyDashboard(Model model) {
        // TODO: Добави статистики, напр. брой служители, брой сгради
        // model.addAttribute("employeeCount", ...);
        // model.addAttribute("buildingCount", ...);

        return "company/index";
    }

    // Тук ще добавим по-късно /employees, /buildings и т.н.
}