package vn.edu.uit.is208.salon.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.uit.is208.salon.dto.BillDto;
import vn.edu.uit.is208.salon.entity.Bill;

@Mapper(componentModel = "spring", uses = BillDetailMapper.class)
public interface BillMapper {
    @Mapping(source = "appointment.id", target = "appointmentId")
    @Mapping(source = "customer.id", target = "customerId")
    BillDto toDto(Bill bill);
}
