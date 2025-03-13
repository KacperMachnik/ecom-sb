package com.ecommerce.service;


import com.ecommerce.exceptions.APIException;
import com.ecommerce.exceptions.ResourceNotFoundException;
import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.payload.ProductDTO;
import com.ecommerce.payload.ProductResponse;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final FileService fileService;
    private final ModelMapper modelMapper;
    @Value("${project.image}")
    private String path;

    @Autowired
    public ProductServiceImpl(CategoryRepository categoryRepository, ProductRepository productRepository, ModelMapper modelMapper, FileService fileService) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
        this.fileService = fileService;
    }

    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {
        Product product = modelMapper.map(productDTO, Product.class);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));


        if (productRepository.existsByProductNameAndCategory(product.getProductName(), category)) {
            throw new APIException(String.format("Product %s already exists in %s category",
                    product.getProductName(), category.getCategoryName()));
        }

        product.setImage("default_img.png");
        product.setCategory(category);
        double specialPrice = product.getPrice() - ((product.getDiscount() / 100 * product.getPrice()));
        product.setSpecialPrice(specialPrice);
        Product savedProduct = productRepository.save(product);
        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> productsPage = productRepository.findAll(pageDetails);
        List<Product> products = productsPage.getContent();
        if (products.isEmpty()) {
            throw new APIException("No products found");
        }
        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class)).toList();
        return new ProductResponse(
                productDTOS,
                productsPage.getNumber(),
                productsPage.getSize(),
                productsPage.getTotalElements(),
                productsPage.getTotalPages(),
                productsPage.isLast()
        );
    }


    @Override
    public ProductResponse findByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> productsPage = productRepository.findByCategoryOrderByPriceAsc(category, pageDetails);
        List<Product> products = productsPage.getContent();
        if (products.isEmpty()) {
            throw new APIException("No products found for category: " + category.getCategoryName());
        }
        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class)).toList();
        return new ProductResponse(
                productDTOS,
                productsPage.getNumber(),
                productsPage.getSize(),
                productsPage.getTotalElements(),
                productsPage.getTotalPages(),
                productsPage.isLast()
        );
    }

    @Override
    public ProductResponse findProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> productsPage = productRepository.findByProductNameLikeIgnoreCase('%' + keyword + '%', pageDetails);
        List<Product> products = productsPage.getContent();
        if (products.isEmpty()) {
            throw new APIException("No products found for keyword: " + keyword);
        }
        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class)).toList();
        return new ProductResponse(
                productDTOS,
                productsPage.getNumber(),
                productsPage.getSize(),
                productsPage.getTotalElements(),
                productsPage.getTotalPages(),
                productsPage.isLast()
        );
    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        log.debug("Updating product");

        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product with Id [{}] not found", productId);
                    return new ResourceNotFoundException("Product", "productId", productId);
                });
        Category category = existingProduct.getCategory();
        if (productRepository.existsByProductNameAndCategoryAndProductIdNot(
                productDTO.getProductName(), category, productId)) {
            throw new APIException(String.format("Another product with name %s already exists in %s category",
                    productDTO.getProductName(), category.getCategoryName()));
        }
        Product product = modelMapper.map(productDTO, Product.class);
        product.setProductId(productId);
        product.setCategory(category);
        if (product.getImage() == null || product.getImage().isEmpty()) {
            product.setImage(existingProduct.getImage());
        }
        double specialPrice = product.getPrice() - ((product.getDiscount() / 100 * product.getPrice()));
        product.setSpecialPrice(specialPrice);
        log.debug("Product category with [{}] Id updated", productId);
        return modelMapper.map(productRepository.save(product), ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        log.debug("Deleting product");
        Product product = productRepository.findById(productId).orElseThrow(() -> {
            log.warn("Product with Id [{}] not found", productId);
            return new ResourceNotFoundException("Product", "productId", productId);
        });
        productRepository.deleteById(productId);
        log.debug("Deleted product with [{}] Id", productId);
        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        log.debug("Updating product image");
        Product product = productRepository.findById(productId).orElseThrow(() -> {
            log.warn("Product with Id [{}] not found", productId);
            return new ResourceNotFoundException("Product", "productId", productId);
        });
        String fileName = fileService.uploadImage(path, image);

        product.setImage(fileName);
        productRepository.save(product);
        return modelMapper.map(product, ProductDTO.class);
    }

}
