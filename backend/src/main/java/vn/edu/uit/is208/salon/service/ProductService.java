package vn.edu.uit.is208.salon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.uit.is208.salon.dto.ProductRequest;
import vn.edu.uit.is208.salon.dto.ProductResponse;
import vn.edu.uit.is208.salon.exception.BusinessRuleException;
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
        String normalizedName = request.getName().trim();

        if (productRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new BusinessRuleException("Tên sản phẩm '" + normalizedName + "' đã tồn tại trong hệ thống");
        }

        Product product = productMapper.toEntity(request);
        product.setName(normalizedName);

        if (product.getBaseUom().equalsIgnoreCase(product.getPurchasingUom())) {
            product.setConversionFactor(BigDecimal.ONE);
        } else {
            if (product.getConversionFactor() == null || product.getConversionFactor().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessRuleException("Hệ số quy đổi phải lớn hơn 0 khi Đơn vị nhập và Đơn vị bán khác nhau.");
            }
        }

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
}