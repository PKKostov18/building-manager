package com.housemanager.service;

import com.housemanager.model.Apartment;
import com.housemanager.model.Building;
import com.housemanager.model.Company;
import org.springframework.stereotype.Service;

@Service
public class TaxService {

    public double calculateMonthlyFee(Apartment apartment) {
        Building building = apartment.getBuilding();
        Company company = building.getCompany();

        double ratePerSqM = company.getDefaultTaxPerSqM();
        double rateElevator = company.getDefaultElevatorTax();
        double ratePet = company.getDefaultPetTax();

        double total = 0.0;

        total += apartment.getArea() * ratePerSqM;

        if (apartment.getResidents() != null) {
            long elevatorUsers = apartment.getResidents().stream()
                    .filter(r -> r.getAge() > 7 && r.isUsesElevator())
                    .count();

            total += elevatorUsers * rateElevator;
        }

        if (apartment.isHasPet()) {
            total += ratePet;
        }

        return total;
    }
}