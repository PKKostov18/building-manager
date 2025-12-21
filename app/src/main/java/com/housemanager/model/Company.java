package com.housemanager.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "companies")
@Getter
@Setter
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(unique = true)
    private String bulstat;

    private String address;

    private String contactPerson;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "default_tax_per_sqm")
    private double defaultTaxPerSqM;

    @Column(name = "default_elevator_tax")
    private double defaultElevatorTax;

    @Column(name = "default_pet_tax")
    private double defaultPetTax;
}