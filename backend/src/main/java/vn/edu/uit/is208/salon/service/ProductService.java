package vn.edu.uit.is208.salon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.uit.is208.salon.dto.ProductRequest;
import vn.edu.uit.is208.salon.dto.ProductResponse;
import vn.edu.uit.is208.salon.entity.Product;
import vn.edu.uit.is208.salon.repository.ProductRepository;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();
        mapToEntity(request, product);
        product.setQuantityOnHand(BigDecimal.ZERO);
        return mapToResponse(productRepository.save(product));
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

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .productType(product.getProductType().name())
                .category(product.getCategory())
                .price(product.getPrice())
                .baseUom(product.getBaseUom())
                .quantityOnHand(product.getQuantityOnHand())
                .build();
    }
}