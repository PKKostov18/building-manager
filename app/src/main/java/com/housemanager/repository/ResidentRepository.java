package com.housemanager.repository;

import com.housemanager.model.Apartment;
import com.housemanager.model.Resident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResidentRepository extends JpaRepository<Resident, Long> {

    List<Resident> findAllByApartment(Apartment apartment);
}