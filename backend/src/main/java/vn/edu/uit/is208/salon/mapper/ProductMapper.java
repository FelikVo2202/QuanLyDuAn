package vn.edu.uit.is208.salon.mapper;

import org.mapstruct.Mapper;
import vn.edu.uit.is208.salon.dto.ProductRequest;
import vn.edu.uit.is208.salon.dto.ProductResponse;
import vn.edu.uit.is208.salon.entity.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductResponse toResponse(Product product);
    Product toEntity(ProductRequest request);
}
