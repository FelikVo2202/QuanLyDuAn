package vn.edu.uit.is208.salon.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.uit.is208.salon.dto.InventoryLedgerResponse;
import vn.edu.uit.is208.salon.entity.InventoryLedger;

@Mapper(componentModel = "spring")
public interface InventoryLedgerMapper {
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    InventoryLedgerResponse toResponse(InventoryLedger inventoryLedger);
}
