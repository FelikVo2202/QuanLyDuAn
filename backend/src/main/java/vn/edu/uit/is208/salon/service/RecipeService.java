package vn.edu.uit.is208.salon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.uit.is208.salon.constant.ProductType;
import vn.edu.uit.is208.salon.dto.CreateRecipeRequest;
import vn.edu.uit.is208.salon.dto.RecipeItemResponse;
import vn.edu.uit.is208.salon.dto.RecipeResponse;
import vn.edu.uit.is208.salon.entity.Product;
import vn.edu.uit.is208.salon.entity.ServiceRecipe;
import vn.edu.uit.is208.salon.entity.SalonService;
import vn.edu.uit.is208.salon.exception.BusinessRuleException;
import vn.edu.uit.is208.salon.exception.ResourceNotFoundException;
import vn.edu.uit.is208.salon.mapper.ServiceRecipeMapper;
import vn.edu.uit.is208.salon.repository.ProductRepository;
import vn.edu.uit.is208.salon.repository.SalonServiceRepository;
import vn.edu.uit.is208.salon.repository.ServiceRecipeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeService {
    private final ServiceRecipeRepository serviceRecipeRepository;
    private final SalonServiceRepository salonServiceRepository;
    private final ProductRepository productRepository;
    private final ServiceRecipeMapper serviceRecipeMapper;

    @Transactional
    public RecipeResponse createRecipe(Long serviceId, CreateRecipeRequest request) {
        SalonService service = salonServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ ID: " + serviceId));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm ID: " + request.getProductId()));

        if (product.getProductType() == ProductType.RETAIL) {
            throw new BusinessRuleException("Sản phẩm '" + product.getName() + "' là hàng chỉ bán lẻ (RETAIL), không thể dùng làm vật tư tiêu hao");
        }

        if (serviceRecipeRepository.existsByIdServiceIdAndIdProductId(serviceId, request.getProductId())) {
            throw new BusinessRuleException("Sản phẩm '" + product.getName() + "' đã tồn tại trong công thức của dịch vụ này.");
        }

        ServiceRecipe recipe = new ServiceRecipe();
        recipe.setService(service);
        recipe.setProduct(product);
        recipe.setQuantityConsumed(request.getQuantityConsumed());

        serviceRecipeRepository.save(recipe);

        return getRecipesByServiceId(serviceId);
    }

    @Transactional(readOnly = true)
    public RecipeResponse getRecipesByServiceId(Long serviceId) {
        SalonService service = salonServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ ID: " + serviceId));

        List<RecipeItemResponse> ingredients = serviceRecipeRepository.findByServiceId(serviceId)
                .stream()
                .map(serviceRecipeMapper::toItemResponse)
                .toList();

        return RecipeResponse.builder()
                .serviceId(service.getId())
                .serviceName(service.getName())
                .ingredients(ingredients)
                .build();
    }
}