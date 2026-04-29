package vn.edu.uit.is208.salon.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.edu.uit.is208.salon.dto.BillDetailDto;
import vn.edu.uit.is208.salon.entity.BillDetail;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface BillDetailMapper {
    @Mapping(source = "service.id", target = "serviceId")
    @Mapping(source = "product.id", target = "productId")
    BillDetailDto toDto(BillDetail billDetail);

    @AfterMapping
    default void calculateLineTotal(@MappingTarget BillDetailDto dto, BillDetail entity) {
        if (entity.getQuantity() != null && entity.getUnitPrice() != null) {
            dto.setLineTotal(entity.getUnitPrice().multiply(BigDecimal.valueOf(entity.getQuantity())));
        }
    }
}
