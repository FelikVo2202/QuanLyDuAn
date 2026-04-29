package vn.edu.uit.is208.salon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.uit.is208.salon.dto.StockUpdateRequest;
import vn.edu.uit.is208.salon.entity.Product;
import vn.edu.uit.is208.salon.entity.Recipe;
import vn.edu.uit.is208.salon.exception.ResourceNotFoundException;
import vn.edu.uit.is208.salon.repository.ProductRepository;
import vn.edu.uit.is208.salon.repository.RecipeRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final ProductRepository productRepository;
    private final RecipeRepository recipeRepository;

    @Transactional
    public void deductStockAfterService(Long serviceId) {
        List<Recipe> recipes = recipeRepository.findByServiceId(serviceId);
        for (Recipe recipe : recipes) {
            Product product = recipe.getProduct();
            BigDecimal amountNeeded = BigDecimal.valueOf(recipe.getAmountNeeded());

            if (product.getQuantityOnHand().compareTo(amountNeeded) < 0) {
                throw new RuntimeException("Không đủ hàng trong kho: " + product.getName());
            }

            product.setQuantityOnHand(product.getQuantityOnHand().subtract(amountNeeded));
            productRepository.save(product);
        }
    }

    @Transactional
    public void addStock(StockUpdateRequest request) {
        Product product = productRepository.findById(request.getProductId()).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

        product.setQuantityOnHand(product.getQuantityOnHand().add(request.getQuantity()));
        productRepository.save(product);
    }
}