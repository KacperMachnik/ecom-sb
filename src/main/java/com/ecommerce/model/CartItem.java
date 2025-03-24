package com.ecommerce.model;

import jakarta.persistence.*;
import lombok.*;

@Entity(name = "cart_items")
@Data
@ToString(exclude = {"product", "cart"})
@EqualsAndHashCode(exclude = {"product", "cart"})
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartItemId;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantity;

    private Double discount;
    private Double price;

    public CartItem(Cart cart, Product product, Integer quantity, Double discount, Double price) {
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
        this.discount = discount;
        this.price = price;
    }
}
