package com.housemanager.repository;

import com.housemanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Boolean existsByEmail(String email);
    Boolean existsByUsername(String username);
    List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String email);
    @Query("SELECT u FROM User u WHERE u.id NOT IN (SELECT a.owner.id FROM Apartment a WHERE a.owner IS NOT NULL)")
    List<User> findAvailableOwners();
}