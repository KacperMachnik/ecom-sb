package com.ecommerce.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity(name = "carts")
@Data
@ToString(exclude = {"cartItemList", "user"})
@EqualsAndHashCode(exclude = {"cartItemList", "user"})
@AllArgsConstructor
@NoArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartId;


    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;


    @OneToMany(mappedBy = "cart", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CartItem> cartItems = new ArrayList<>();


    private Double totalPrice = 0.0;

    public Cart(User user, Double totalPrice) {
        this.user = user;
        this.totalPrice = totalPrice;
    }
}
