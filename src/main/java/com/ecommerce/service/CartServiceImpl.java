package com.ecommerce.service;

import com.ecommerce.exceptions.APIException;
import com.ecommerce.exceptions.ResourceNotFoundException;
import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import com.ecommerce.payload.dto.CartDTO;
import com.ecommerce.payload.dto.ProductDTO;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.util.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@Slf4j
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final AuthUtil authUtil;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, AuthUtil authUtil, ProductRepository productRepository, CartItemRepository cartItemRepository, ModelMapper modelMapper) {
        this.cartRepository = cartRepository;
        this.authUtil = authUtil;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        Cart cart = createCart();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        Optional<CartItem> cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId, cart.getCartId());

        if (cartItem.isPresent()) {
            throw new APIException("Product" + product.getProductName() + " already exists in the cart");
        }
        if (product.getQuantity() == 0) {
            throw new APIException("Product" + product.getProductName() + " is not available");
        }
        if (product.getQuantity() < quantity) {
            throw new APIException("Please make an order of the " + product.getProductName() + " less than or equal to the quantity" + quantity);
        }

        CartItem newCartItem = new CartItem(cart, product, quantity, product.getDiscount(), product.getSpecialPrice());
        cart.getCartItems().add(newCartItem);
        cartItemRepository.save(newCartItem);

        product.setQuantity(product.getQuantity());

        cart.setTotalPrice(cart.getTotalPrice() + (newCartItem.getPrice() * quantity));
        cartRepository.save(cart);
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();
        log.error(cartItems.toString());
        //does have quantity or no?
        return getCartDTOWithProductDTOS(cartDTO, cartItems);
    }

    @Override
    public CartDTO getCartByEmail() {
        String email = authUtil.loggedInEmail();
        Cart cart = cartRepository.findCartByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Cart", "email", email));

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        cart.getCartItems().forEach(c -> c.getProduct().setQuantity(c.getQuantity()));
        List<ProductDTO> productDTOS = cart.getCartItems().stream().map(p ->
                modelMapper.map(p.getProduct(), ProductDTO.class)).toList();
        cartDTO.setProducts(productDTOS);
        return cartDTO;
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();

        if (carts.isEmpty()) {
            throw new APIException("No carts found");
        }
        return carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

            List<ProductDTO> productDTOS = cart.getCartItems()
                    .stream()
                    .map(product -> modelMapper.map(product.getProduct(), ProductDTO.class))
                    .toList();
            cartDTO.setProducts(productDTOS);
            return cartDTO;
        }).toList();
    }

    @Override
    @Transactional
    public CartDTO updateProductQuantityInCart(Long productId, Integer amount) {
        String email = authUtil.loggedInEmail();
        Cart userCart = cartRepository.findCartByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "emailid", email));
        Long cartId = userCart.getCartId();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        if (product.getQuantity() == 0) {
            throw new APIException("Product " + product.getProductName() + " is not available");
        }

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId)
                .orElseThrow(() -> new APIException("Product " + product.getProductName() + " is not in the cart"));
        if (cartItem.getQuantity() + amount < 0) {
            throw new APIException("The resulting quantity cannot be negative!");
        }

        if (cartItem.getQuantity() + amount == 0) {
            deleteProductFromCart(cartId, productId);
            userCart.getCartItems().remove(cartItem);
        } else {
            cartItem.setQuantity(cartItem.getQuantity() + amount);
            cartItemRepository.save(cartItem);
            userCart.setTotalPrice(userCart.getTotalPrice() + (cartItem.getPrice() * amount));
        }
        cartRepository.save(userCart);
        CartItem updatedItem = cartItemRepository.save(cartItem);
        if (updatedItem.getQuantity() <= 0) {
            cartItemRepository.delete(updatedItem);
        }

        CartDTO cartDTO = modelMapper.map(userCart, CartDTO.class);
        List<CartItem> cartItems = userCart.getCartItems();
        return getCartDTOWithProductDTOS(cartDTO, cartItems);
    }

    @Override
    @Transactional
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", cartId));
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId, cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "productId", productId));
        cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getPrice() * cartItem.getQuantity()));

        cartItemRepository.deleteCartItemByProductIdAndCartId(productId, cartId);
        //TODO change return type to something else??
        return "Product " + cartItem.getProduct().getProductName() + " removed from cart";
    }

    @Override
    @Transactional
    //update total price - if price of item changes hibernate wont see that at propagate it to that field
    public void updateProductInCarts(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", cartId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId, cartId)
                .orElseThrow(() -> new APIException("Product " + product.getProductName() + " is not in the cart"));

        double cartTotalPrice = cart.getTotalPrice() - (cartItem.getPrice() * cartItem.getQuantity());
        cartItem.setPrice(product.getSpecialPrice());
        cart.setTotalPrice(cartTotalPrice + (cartItem.getPrice() * product.getQuantity()));
        cartItemRepository.save(cartItem);
    }

    private CartDTO getCartDTOWithProductDTOS(CartDTO cartDTO, List<CartItem> cartItems) {
        Stream<ProductDTO> productDTOStream = cartItems.stream().map(item -> {
            ProductDTO productDTO = modelMapper.map(item.getProduct(), ProductDTO.class);
            productDTO.setQuantity(item.getQuantity());
            return productDTO;
        });
        cartDTO.setProducts(productDTOStream.toList());
        return cartDTO;
    }

    private Cart createCart() {
        Cart cart = cartRepository.findCartByEmail(authUtil.loggedInEmail())
                .orElseGet(() -> new Cart(authUtil.loggedInUser(), 0.00));
        return cartRepository.save(cart);
    }
}
