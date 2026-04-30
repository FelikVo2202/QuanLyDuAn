package vn.edu.uit.is208.salon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.uit.is208.salon.dto.StockUpdateRequest;
import vn.edu.uit.is208.salon.entity.Product;
import vn.edu.uit.is208.salon.exception.ResourceNotFoundException;
import vn.edu.uit.is208.salon.repository.ProductRepository;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final ProductRepository productRepository;

    @Transactional
    public void addStock(StockUpdateRequest request) {
        Product product = productRepository.findById(request.getProductId()).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

        product.setQuantityOnHand(product.getQuantityOnHand().add(request.getQuantity()));
        productRepository.save(product);
    }
}