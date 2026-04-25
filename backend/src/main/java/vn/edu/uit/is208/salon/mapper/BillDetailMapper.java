package vn.edu.uit.is208.salon.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.uit.is208.salon.dto.BillDetailDto;
import vn.edu.uit.is208.salon.entity.BillDetail;

@Mapper(componentModel = "spring")
public interface BillDetailMapper {
    @Mapping(source = "service.id", target = "serviceId")
    @Mapping(source = "product.id", target = "productId")
    BillDetailDto toDto(BillDetail billDetail);
}
