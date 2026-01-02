package com.housemanager.repository;

import com.housemanager.model.Apartment;
import com.housemanager.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByApartmentOrderByPaymentDateDesc(Apartment apartment);
    boolean existsByApartmentAndForMonthAndForYear(Apartment apartment, int forMonth, int forYear);
}