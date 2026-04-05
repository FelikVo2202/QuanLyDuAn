package vn.edu.uit.is208.salon.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import vn.edu.uit.is208.salon.dto.CreateCustomerRequest;
import vn.edu.uit.is208.salon.dto.UpdateCustomerRequest;
import vn.edu.uit.is208.salon.dto.CustomerResponse;
import vn.edu.uit.is208.salon.entity.Customer;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    Customer toEntity(CreateCustomerRequest request);

    CustomerResponse toResponse(Customer customer);

    void updateEntityFromDto(UpdateCustomerRequest dto, @MappingTarget Customer entity);
}