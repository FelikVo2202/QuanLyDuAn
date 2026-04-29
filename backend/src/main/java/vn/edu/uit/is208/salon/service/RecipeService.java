package vn.edu.uit.is208.salon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.uit.is208.salon.dto.CreateRecipeRequest;
import vn.edu.uit.is208.salon.dto.RecipeResponse;
import vn.edu.uit.is208.salon.entity.Product;
import vn.edu.uit.is208.salon.entity.ServiceRecipe;
import vn.edu.uit.is208.salon.entity.SalonService;
import vn.edu.uit.is208.salon.exception.ResourceNotFoundException;
import vn.edu.uit.is208.salon.repository.ProductRepository;
import vn.edu.uit.is208.salon.repository.RecipeRepository;
import vn.edu.uit.is208.salon.repository.SalonServiceRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final SalonServiceRepository salonServiceRepository;
    private final ProductRepository productRepository;

    @Transactional
    public RecipeResponse createRecipe(Long serviceId, CreateRecipeRequest request) {
        SalonService service = salonServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

        ServiceRecipe recipe = new ServiceRecipe();
        recipe.setService(service);
        recipe.setProduct(product);
        recipe.setQuantityConsumed(request.getQuantityConsumed());

        ServiceRecipe savedRecipe = recipeRepository.save(recipe);
        return convertToResponse(savedRecipe);
    }

    public List<RecipeResponse> getRecipesByServiceId(Long serviceId) {
        return recipeRepository.findByServiceId(serviceId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    private RecipeResponse convertToResponse(ServiceRecipe recipe) {
        return RecipeResponse.builder()
                .serviceId(recipe.getService().getId())
                .serviceName(recipe.getService().getName())
                .productId(recipe.getProduct().getId())
                .productName(recipe.getProduct().getName())
                .quantityConsumed(recipe.getQuantityConsumed())
                .baseUom(recipe.getProduct().getBaseUom())
                .build();
    }
}