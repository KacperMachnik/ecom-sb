package com.ecommerce.service;

import com.ecommerce.payload.dto.CartDTO;

import java.util.List;

public interface CartService {
    CartDTO getCartByEmail();

    CartDTO addProductToCart(Long productId, Integer quantity);

    List<CartDTO> getAllCarts();

    CartDTO updateProductQuantityInCart(Long productId, Integer amount);

    String deleteProductFromCart(Long cartId, Long productId);

    void updateProductInCarts(Long cartId, Long productId);
}
