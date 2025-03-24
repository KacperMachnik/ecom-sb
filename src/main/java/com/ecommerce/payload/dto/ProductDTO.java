package com.ecommerce.payload.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long productId;

    @NotBlank
    @Size(min = 3, message = "Product name must contain at least 3 characters")
    private String productName;

    private String image;
    private String description;

    @PositiveOrZero(message = "Quantity must be zero or positive")
    private Integer quantity;

    @Positive(message = "Price must be positive")
    private double price;

    @Min(value = 0, message = "Discount cannot be less than 0")
    @Max(value = 100, message = "Discount cannot be more than 100")
    private double discount;

    @Positive(message = "Special price must be positive")
    private double specialPrice;

    public void setProductName(String categoryName) {
        this.productName = categoryName != null ? categoryName.trim() : null;
    }
}
