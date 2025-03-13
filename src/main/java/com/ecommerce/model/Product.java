package com.ecommerce.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity(name = "products")
@Data
@ToString(exclude = "category")
@EqualsAndHashCode(exclude = "category")
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long productId;
    private String productName;
    private String image;
    private String description;
    private Integer quantity;
    private double price;
    private double discount;
    private double specialPrice;


    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonBackReference
    private Category category;

}
