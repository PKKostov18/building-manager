package com.housemanager.service;

import com.housemanager.model.Apartment;
import com.housemanager.model.Company;
import com.housemanager.model.Payment;
import com.housemanager.model.User;
import com.housemanager.repository.ApartmentRepository;
import com.housemanager.repository.CompanyRepository;
import com.housemanager.repository.PaymentRepository;
import com.housemanager.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ApartmentRepository apartmentRepository;
    private final PaymentRepository paymentRepository;
    private final CompanyRepository companyRepository;

    public UserService(UserRepository userRepository,
                       ApartmentRepository apartmentRepository,
                       PaymentRepository paymentRepository,
                       CompanyRepository companyRepository) {
        this.userRepository = userRepository;
        this.apartmentRepository = apartmentRepository;
        this.paymentRepository = paymentRepository;
        this.companyRepository = companyRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Transactional
    public void updateUser(Long id, User updatedUser) {
        User existingUser = findById(id);
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());
        userRepository.save(existingUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = findById(id);

        Company company = companyRepository.findByUser(user);

        if (company != null) {
            company.setUser(null);
            companyRepository.save(company);
        }

        List<Apartment> ownedApartments = apartmentRepository.findByOwnerId(id);
        for (Apartment apt : ownedApartments) {
            apt.setOwner(null);
            apartmentRepository.save(apt);
        }

        List<Payment> payments = paymentRepository.findByPayer(user);
        if (!payments.isEmpty()) {
            paymentRepository.deleteAll(payments);
        }

        userRepository.delete(user);
    }
}