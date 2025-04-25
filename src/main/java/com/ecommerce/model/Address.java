package com.ecommerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity(name = "addresses")
@ToString(exclude = {"users"})
@EqualsAndHashCode(exclude = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @NotBlank
    @Size(min = 5, message = "Street name must be at least 5 characters")
    private String street;

    @NotBlank
    @Size(min = 5, message = "Building name must be at least 5 characters")
    private String buildingName;

    @NotBlank
    @Size(min = 3, message = "City name must be at least 3 characters")
    private String city;

    @NotBlank
    @Size(min = 3, message = "State name must be at least 3 characters")
    private String State;

    @NotBlank
    @Size(min = 3, message = "Country name must be at least 3 characters")
    private String Country;

    @NotBlank
    @Size(min = 6, max = 6, message = "Zip Code name must be exactly 6 characters")
    private String zipCode;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
