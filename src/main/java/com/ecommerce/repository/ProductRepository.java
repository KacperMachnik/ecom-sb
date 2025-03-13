package com.ecommerce.repository;

import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByProductNameAndCategoryAndProductIdNot(String productName, Category category, Long productId);

    boolean existsByProductNameAndCategory(String productName, Category category);

    Page<Product> findByCategoryOrderByPriceAsc(Category category, Pageable pageDetails);

    Page<Product> findByProductNameLikeIgnoreCase(String s, Pageable pageDetails);
}
