package com.ecommerce.service;

import com.ecommerce.model.Category;
import com.ecommerce.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public void createCategory(Category category) {
        categoryRepository.save(category);
    }

    @Override
    public String deleteCategory(Long categoryId) {
       categoryRepository.findById(categoryId).orElseThrow(() -> {
            LOGGER.error("Category with ID [{}] not found",categoryId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        });
        categoryRepository.deleteById(categoryId);
        return "Category with categoryId " + categoryId + " deleted";
    }

    @Override
    public Category updateCategory(Category category, Long categoryId) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() ->{
            LOGGER.error("Category with ID [{}] not found",categoryId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");
        });

        category.setCategoryId(categoryId);
        return categoryRepository.save(category);
    }

}
