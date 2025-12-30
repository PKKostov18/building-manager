package com.housemanager.controller;

import com.housemanager.dto.ResidentRegistrationDto;
import com.housemanager.repository.ResidentRepository;
import com.housemanager.service.ResidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/employee/residents")
public class EmployeeResidentController {

    @Autowired private ResidentService residentService;
    @Autowired private ResidentRepository residentRepository;

    @GetMapping
    public String listResidents(Model model) {
        model.addAttribute("residents", residentRepository.findAll());
        return "employee/residents-list";
    }

    @GetMapping("/add")
    public String showAddResidentForm(Model model) {
        model.addAttribute("residentDto", new ResidentRegistrationDto());
        return "employee/resident-form";
    }

    @PostMapping("/add")
    public String addResident(@ModelAttribute("residentDto") ResidentRegistrationDto dto, Model model) {
        try {
            residentService.createResident(dto);
            return "redirect:/employee/residents";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());

            return "employee/resident-form";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "employee/resident-form";
        }
    }
}