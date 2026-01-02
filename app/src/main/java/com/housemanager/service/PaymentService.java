package com.housemanager.service;

import com.housemanager.model.*;
import com.housemanager.repository.PaymentRepository;
import com.housemanager.repository.ResidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

@Service
public class PaymentService {

    @Autowired private PaymentRepository paymentRepository;
    @Autowired private ResidentRepository residentRepository;
    @Autowired private TaxService taxService;

    private static final String PAYMENT_FILE = "payments_log.csv";

    @Transactional
    public void payMonthlyFee(User payer) {
        Resident resident = residentRepository.findByUser(payer);
        if (resident == null || resident.getApartment() == null) {
            throw new RuntimeException("No apartment assigned to this user.");
        }

        Apartment apartment = resident.getApartment();
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        if (paymentRepository.existsByApartmentAndForMonthAndForYear(apartment, currentMonth, currentYear)) {
            throw new RuntimeException("Fee for this month is already paid!");
        }

        double amount = taxService.calculateMonthlyFee(apartment);

        Payment payment = new Payment();
        payment.setAmount(amount);
        payment.setPaymentDate(now);
        payment.setForMonth(currentMonth);
        payment.setForYear(currentYear);
        payment.setApartment(apartment);
        payment.setPayer(payer);

        paymentRepository.save(payment);

        writePaymentToFile(payment, apartment);
    }

    private void writePaymentToFile(Payment payment, Apartment apartment) {
        Building building = apartment.getBuilding();
        Employee employee = building.getEmployee();
        Company company = building.getCompany();

        String line = String.format("%s | %s | %s | %s | %.2f BGN | %s",
                company.getName(),
                (employee != null ? employee.getName() : "No Employee"),
                building.getAddress(),
                apartment.getNumber(),
                payment.getAmount(),
                payment.getPaymentDate().toString()
        );

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PAYMENT_FILE, true))) {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not write payment to file!");
        }
    }

    public boolean isPaidForCurrentMonth(Apartment apartment) {
        LocalDate now = LocalDate.now();
        return paymentRepository.existsByApartmentAndForMonthAndForYear(
                apartment, now.getMonthValue(), now.getYear());
    }
}