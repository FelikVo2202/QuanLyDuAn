package vn.edu.uit.is208.salon.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.uit.is208.salon.dto.RecipeItemResponse;
import vn.edu.uit.is208.salon.dto.RecipeResponse;
import vn.edu.uit.is208.salon.entity.ServiceRecipe;

@Mapper(componentModel = "spring")
public interface ServiceRecipeMapper {
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "baseUom", source = "product.baseUom")
    RecipeItemResponse toItemResponse(ServiceRecipe request);
}
