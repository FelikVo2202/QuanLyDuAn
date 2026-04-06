package vn.edu.uit.is208.salon.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.uit.is208.salon.dto.CreateStaffRequest;
import vn.edu.uit.is208.salon.dto.StaffDto;
import vn.edu.uit.is208.salon.entity.Staff;

@Mapper(componentModel = "spring")
public interface StaffMapper {
    @Mapping(target = "passwordHash", ignore = true)
    Staff toEntity(CreateStaffRequest request);

    StaffDto toDto(Staff staff);
}
