package com.ecommerce.payload.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Long categoryId;
    @NotBlank
    @Size(min = 3, message = "Category name must contain at least 3 characters")
    private String categoryName;

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName != null ? categoryName.trim() : null;
    }
}
