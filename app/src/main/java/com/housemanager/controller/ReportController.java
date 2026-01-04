package com.housemanager.controller;

import com.housemanager.model.Apartment;
import com.housemanager.model.Building;
import com.housemanager.model.Company;
import com.housemanager.model.Employee;
import com.housemanager.model.User;
import com.housemanager.repository.BuildingRepository;
import com.housemanager.repository.CompanyRepository;
import com.housemanager.repository.EmployeeRepository;
import com.housemanager.repository.UserRepository;
import com.housemanager.service.PaymentService;
import com.housemanager.service.TaxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/company/reports")
public class ReportController {

    @Autowired private UserRepository userRepository;
    @Autowired private CompanyRepository companyRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private BuildingRepository buildingRepository;
    @Autowired private TaxService taxService;
    @Autowired private PaymentService paymentService;

    @GetMapping
    public String showReports(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User adminUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Company company = companyRepository.findByUser(adminUser);

        if (company == null) {
            return "redirect:/";
        }

        List<Employee> employees = employeeRepository.findAllByCompany(company);
        List<Building> buildings = buildingRepository.findAllByCompany(company);

        double companyTotalExpected = 0.0;
        double companyTotalCollected = 0.0;

        Map<Long, BuildingStats> buildingStatsMap = new HashMap<>();

        for (Building b : buildings) {
            double bExpected = 0.0;
            double bCollected = 0.0;
            int residentCount = 0;

            for (Apartment apt : b.getApartments()) {
                if (apt.getResidents() != null) {
                    residentCount += apt.getResidents().size();
                }

                double fee = taxService.calculateMonthlyFee(apt);
                boolean isOccupied = (apt.getOwner() != null || (apt.getResidents() != null && !apt.getResidents().isEmpty()));

                if (isOccupied && fee > 0) {
                    bExpected += fee;
                    if (paymentService.isPaidForCurrentMonth(apt)) {
                        bCollected += fee;
                    }
                }
            }

            companyTotalExpected += bExpected;
            companyTotalCollected += bCollected;

            buildingStatsMap.put(b.getId(), new BuildingStats(
                    b.getApartments().size(),
                    residentCount,
                    bExpected,
                    bCollected
            ));
        }

        List<EmployeeStats> employeeStatsList = new ArrayList<>();

        for (Employee emp : employees) {
            List<Building> empBuildings = buildingRepository.findByEmployee(emp);
            double empExpected = 0.0;
            double empCollected = 0.0;

            for (Building b : empBuildings) {
                if (buildingStatsMap.containsKey(b.getId())) {
                    BuildingStats bs = buildingStatsMap.get(b.getId());
                    empExpected += bs.expected;
                    empCollected += bs.collected;
                }
            }

            employeeStatsList.add(new EmployeeStats(emp, empBuildings, empExpected, empCollected));
        }

        List<Building> unassignedBuildings = new ArrayList<>();
        for (Building b : buildings) {
            if (b.getEmployee() == null) {
                unassignedBuildings.add(b);
            }
        }

        model.addAttribute("company", company);

        model.addAttribute("totalExpected", companyTotalExpected);
        model.addAttribute("totalCollected", companyTotalCollected);
        model.addAttribute("collectionRate", (companyTotalExpected > 0) ? (companyTotalCollected / companyTotalExpected) * 100 : 0);

        model.addAttribute("buildings", buildings);
        model.addAttribute("buildingStats", buildingStatsMap);
        model.addAttribute("employeeStats", employeeStatsList);
        model.addAttribute("unassignedBuildings", unassignedBuildings);

        return "company/reports";
    }

    public record BuildingStats(int aptCount, int residentCount, double expected, double collected) {}
    public record EmployeeStats(Employee employee, List<Building> buildings, double expected, double collected) {}
}