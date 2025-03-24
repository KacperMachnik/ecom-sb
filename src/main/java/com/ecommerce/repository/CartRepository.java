package com.ecommerce.repository;

import com.ecommerce.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @Query("SELECT c FROM carts c WHERE c.user.email = ?1")
    Optional<Cart> findCartByEmail(String email);


    @Query("SELECT c from carts c JOIN FETCH c.cartItems ci JOIN ci.product p WHERE p.productId =?1")
    List<Cart> findCartsByProductId(Long productId);
}
