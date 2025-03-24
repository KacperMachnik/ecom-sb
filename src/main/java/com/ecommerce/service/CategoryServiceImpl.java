package com.ecommerce.service;

import com.ecommerce.exceptions.APIException;
import com.ecommerce.exceptions.ResourceNotFoundException;
import com.ecommerce.model.Category;
import com.ecommerce.payload.dto.CategoryDTO;
import com.ecommerce.payload.response.CategoryResponse;
import com.ecommerce.repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    private final ModelMapper modelMapper;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        log.debug("Fetching all categories");

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Category> categoryPage = categoryRepository.findAll(pageDetails);

        List<Category> categories = categoryPage.getContent();
        if (categories.isEmpty()) {
            log.warn("No categories found");
            throw new APIException("There are no categories");
        }
        log.debug("Fetched all categories");
        List<CategoryDTO> categoryDTOS = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();
        return new CategoryResponse(
                categoryDTOS,
                categoryPage.getNumber(),
                categoryPage.getSize(),
                categoryPage.getTotalElements(),
                categoryPage.getTotalPages(),
                categoryPage.isLast()
        );
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        log.debug("Creating category");
        Category category = modelMapper.map(categoryDTO, Category.class);
        categoryRepository.findByCategoryName(category.getCategoryName()).ifPresent(cat -> {
            log.warn("Category with name [{}] already exists", category.getCategoryName());
            throw new APIException(String.format("Category with name %s already exists", category.getCategoryName()));
        });
        return modelMapper.map(categoryRepository.save(category), CategoryDTO.class);
    }

    @Override
    public CategoryDTO deleteCategory(Long categoryId) {
        log.debug("Deleting category");
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> {
            log.warn("Category with Id [{}] not found", categoryId);
            return new ResourceNotFoundException("Category", "categoryId", categoryId);
        });
        categoryRepository.deleteById(categoryId);
        log.debug("Deleted category with [{}] Id", categoryId);
        return modelMapper.map(category, CategoryDTO.class);
    }

    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId) {
        log.debug("Updating category");
        Category category = modelMapper.map(categoryDTO, Category.class);
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("Category with Id [{}] not found", categoryId);
                    return new ResourceNotFoundException("Category", "categoryId", categoryId);
                });

        categoryRepository.findByCategoryName(category.getCategoryName())
                .ifPresent(foundCategory -> {
                    if (!foundCategory.getCategoryId().equals(categoryId)) {
                        log.warn("Category with name [{}] already exists", category.getCategoryName());
                        throw new APIException(String.format("Category with name %s already exists", category.getCategoryName()));
                    }
                });

        category.setCategoryId(categoryId);
        log.debug("Updated category with [{}] Id updated", categoryId);
        return modelMapper.map(categoryRepository.save(category), CategoryDTO.class);
    }
}
