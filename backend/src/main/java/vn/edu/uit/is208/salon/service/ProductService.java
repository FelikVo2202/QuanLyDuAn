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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id)));
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
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));

        String normalizedName = request.getName().trim();
        if (productRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, id)) {
            throw new BusinessRuleException("Tên sản phẩm '" + normalizedName + "' đã tồn tại trong hệ thống");
        }

        boolean isUomChanged = !existingProduct.getBaseUom().equalsIgnoreCase(request.getBaseUom()) ||
                !existingProduct.getPurchasingUom().equalsIgnoreCase(request.getPurchasingUom()) ||
                (existingProduct.getConversionFactor() != null && existingProduct.getConversionFactor().compareTo(request.getConversionFactor()) != 0);

        if (isUomChanged) {
            boolean hasTransactions = inventoryLedgerRepository.existsByProductId(id) || billDetailRepository.existsByProductId(id);
            if (hasTransactions) {
                throw new BusinessRuleException("Không thể thay đổi đơn vị tính hoặc hệ số quy đổi vì sản phẩm này đã phát sinh giao dịch kho hoặc hóa đơn");
            }

            if (request.getBaseUom().equalsIgnoreCase(request.getPurchasingUom())) {
                existingProduct.setConversionFactor(BigDecimal.ONE);
            } else {
                if (request.getConversionFactor() == null || request.getConversionFactor().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BusinessRuleException("Hệ số quy đổi phải lớn hơn 0 khi Đơn vị nhập và Đơn vị bán khác nhau.");
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
                            "Không thể đổi loại sản phẩm thành Bán lẻ (RETAIL) vì sản phẩm này đang được sử dụng làm vật tư tiêu hao trong Dịch vụ"
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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));

        if (product.getQuantityOnHand() != null && product.getQuantityOnHand().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessRuleException(
                    "Không thể xóa sản phẩm khi số lượng tồn kho vẫn còn (" + product.getQuantityOnHand() + ")");
        }

        if (serviceRecipeRepository.existsByProductId(id)) {
            throw new BusinessRuleException(
                    "Không thể xóa sản phẩm vì đang được sử dụng làm vật tư tiêu hao cho một Dịch vụ");
        }

        boolean isLockedInPendingBill = billDetailRepository.existsByProductIdAndBillPaymentStatus(id, PaymentStatus.PENDING);
        if (isLockedInPendingBill) {
            throw new BusinessRuleException("Không thể xóa sản phẩm vì đang có Hóa đơn chưa thanh toán");
        }

        product.setDeletedAt(LocalDateTime.now());
    }
}