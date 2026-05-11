package vn.edu.uit.is208.salon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.uit.is208.salon.constant.PaymentStatus;
import vn.edu.uit.is208.salon.dto.ProductRequest;
import vn.edu.uit.is208.salon.dto.ProductResponse;
import vn.edu.uit.is208.salon.exception.BusinessRuleException;
import vn.edu.uit.is208.salon.exception.ResourceNotFoundException;
import vn.edu.uit.is208.salon.mapper.ProductMapper;
import vn.edu.uit.is208.salon.constant.ProductType;
import vn.edu.uit.is208.salon.entity.Product;
import vn.edu.uit.is208.salon.repository.BillDetailRepository;
import vn.edu.uit.is208.salon.repository.InventoryLedgerRepository;
import vn.edu.uit.is208.salon.repository.ProductRepository;
import vn.edu.uit.is208.salon.repository.ServiceRecipeRepository;
import vn.edu.uit.is208.salon.repository.specifications.ProductSpecification;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final InventoryLedgerRepository inventoryLedgerRepository;
    private final BillDetailRepository billDetailRepository;
    private final ServiceRecipeRepository serviceRecipeRepository;

    public Page<ProductResponse> getAllProducts(ProductType productType, String category, String search, Pageable pageable) {
        return productRepository.findAll(ProductSpecification.withFilters(productType, category, search), pageable)
                .map(productMapper::toResponse);
    }

    public ProductResponse get(Long id) {
        return productMapper.toResponse(productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id)));
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        String normalizedName = request.getName().trim();

        if (productRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new BusinessRuleException("Product name '" + normalizedName + "' already exists in the system");
        }

        Product product = productMapper.toEntity(request);
        product.setName(normalizedName);

        if (product.getBaseUom().equalsIgnoreCase(product.getPurchasingUom())) {
            product.setConversionFactor(BigDecimal.ONE);
        } else {
            if (product.getConversionFactor() == null || product.getConversionFactor().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessRuleException("Conversion factor must be greater than 0 when purchasing UOM and base UOM are different.");
            }
        }

        product.setQuantityOnHand(BigDecimal.ZERO);

        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        String normalizedName = request.getName().trim();
        if (productRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, id)) {
            throw new BusinessRuleException("Product name '" + normalizedName + "' already exists in the system");
        }

        boolean isUomChanged = !existingProduct.getBaseUom().equalsIgnoreCase(request.getBaseUom()) ||
                !existingProduct.getPurchasingUom().equalsIgnoreCase(request.getPurchasingUom()) ||
                (existingProduct.getConversionFactor() != null && existingProduct.getConversionFactor().compareTo(request.getConversionFactor()) != 0);

        if (isUomChanged) {
            boolean hasTransactions = inventoryLedgerRepository.existsByProduct_Id(id) || billDetailRepository.existsByProductId(id);
            if (hasTransactions) {
                throw new BusinessRuleException("Cannot change unit of measure or conversion factor because this product already has inventory or billing transactions");
            }

            if (request.getBaseUom().equalsIgnoreCase(request.getPurchasingUom())) {
                existingProduct.setConversionFactor(BigDecimal.ONE);
            } else {
                if (request.getConversionFactor() == null || request.getConversionFactor().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BusinessRuleException("Conversion factor must be greater than 0 when purchasing UOM and base UOM are different.");
                }
                existingProduct.setConversionFactor(request.getConversionFactor());
            }
            existingProduct.setBaseUom(request.getBaseUom());
            existingProduct.setPurchasingUom(request.getPurchasingUom());
        }

        if (existingProduct.getProductType() != request.getProductType()) {
            if (request.getProductType() == ProductType.RETAIL) {
                boolean isUsedInRecipe = serviceRecipeRepository.existsByProductId(id);
                if (isUsedInRecipe) {
                    throw new BusinessRuleException(
                            "Cannot change product type to RETAIL because this product is used as a consumable ingredient in a service"
                    );
                }
            }
        }

        existingProduct.setName(normalizedName);
        existingProduct.setProductType(request.getProductType());
        existingProduct.setCategory(request.getCategory());
        existingProduct.setPrice(request.getPrice());

        return productMapper.toResponse(productRepository.save(existingProduct));
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        if (product.getQuantityOnHand() != null && product.getQuantityOnHand().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessRuleException(
                    "Cannot delete product while inventory on hand is still greater than 0 (" + product.getQuantityOnHand() + ")");
        }

        if (serviceRecipeRepository.existsByProductId(id)) {
            throw new BusinessRuleException(
                    "Cannot delete product because it is used as a consumable ingredient for a service");
        }

        boolean isLockedInPendingBill = billDetailRepository.existsByProductIdAndBillPaymentStatus(id, PaymentStatus.PENDING);
        if (isLockedInPendingBill) {
            throw new BusinessRuleException("Cannot delete product because it is used in a bill that has not been paid yet");
        }

        product.setDeletedAt(LocalDateTime.now());
    }
}