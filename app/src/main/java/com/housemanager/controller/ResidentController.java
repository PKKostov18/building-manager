package com.housemanager.controller;

import com.housemanager.model.Apartment;
import com.housemanager.model.Payment;
import com.housemanager.model.Resident;
import com.housemanager.model.User;
import com.housemanager.repository.PaymentRepository;
import com.housemanager.repository.ResidentRepository;
import com.housemanager.repository.UserRepository;
import com.housemanager.service.PaymentService;
import com.housemanager.service.TaxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/resident")
public class ResidentController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResidentRepository residentRepository;

    @Autowired
    private TaxService taxService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentService paymentService;

    @GetMapping
    public String showDashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Resident resident = residentRepository.findByUser(user);

        if (resident == null) {
            model.addAttribute("error", "Your account is not linked to a resident profile yet.");
            return "resident/index";
        }

        Apartment apartment = resident.getApartment();
        model.addAttribute("resident", resident);

        if (apartment != null) {
            model.addAttribute("apartment", apartment);
            model.addAttribute("building", apartment.getBuilding());

            double monthlyFee = taxService.calculateMonthlyFee(apartment);
            model.addAttribute("monthlyFee", monthlyFee);

            boolean isPaid = paymentService.isPaidForCurrentMonth(apartment);
            model.addAttribute("isPaid", isPaid);

            List<Payment> payments = paymentRepository.findAllByApartmentOrderByPaymentDateDesc(apartment);
            model.addAttribute("paymentHistory", payments);
        }

        return "resident/index";
    }

    @PostMapping("/pay")
    public String payFee(@AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
            paymentService.payMonthlyFee(user);
            redirectAttributes.addFlashAttribute("success", "Payment successful! Thank you.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/resident";
    }

    @GetMapping("/fees")
    public String showFees(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        return "redirect:/resident";
    }
}