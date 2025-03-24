package com.ecommerce.repository;

import com.ecommerce.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query("SELECT c FROM cart_items c WHERE  c.product.productId = ?1 AND c.cart.cartId = ?2")
    Optional<CartItem> findCartItemByProductIdAndCartId(Long productIdLong, Long cartId);


    @Modifying
    @Query("DELETE FROM cart_items ci WHERE ci.product.productId = ?1 AND ci.cart.cartId = ?2")
    void deleteCartItemByProductIdAndCartId(Long productId, Long cartId);
}
