package vn.edu.uit.is208.salon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.uit.is208.salon.dto.ProductRequest;
import vn.edu.uit.is208.salon.dto.ProductResponse;
import vn.edu.uit.is208.salon.mapper.ProductMapper;
import vn.edu.uit.is208.salon.constant.ProductType;
import vn.edu.uit.is208.salon.entity.Product;
import vn.edu.uit.is208.salon.repository.ProductRepository;
import vn.edu.uit.is208.salon.repository.specifications.ProductSpecification;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public Page<ProductResponse> getAllProducts(ProductType productType, String category, String search, Pageable pageable) {
        return productRepository.findAll(ProductSpecification.withFilters(productType, category, search), pageable)
                .map(productMapper::toResponse);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();
        mapToEntity(request, product);
        product.setQuantityOnHand(BigDecimal.ZERO);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setDeletedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    private void mapToEntity(ProductRequest request, Product product) {
        product.setName(request.getName());
        product.setProductType(request.getProductType());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setBaseUom(request.getBaseUom());
        product.setPurchasingUom(request.getPurchasingUom());
        product.setConversionFactor(request.getConversionFactor());
    }
}