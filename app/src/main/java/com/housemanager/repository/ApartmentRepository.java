package com.housemanager.repository;

import com.housemanager.model.Apartment;
import com.housemanager.model.Building;
import com.housemanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, Long> {

    List<Apartment> findAllByBuilding(Building building);
    List<Apartment> findByBuilding(Building building);
    List<Apartment> findByOwnerId(Long ownerId);
    List<Apartment> findByOwner(User owner);
}