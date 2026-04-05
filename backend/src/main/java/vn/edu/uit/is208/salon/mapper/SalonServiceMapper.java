package vn.edu.uit.is208.salon.mapper;

import org.mapstruct.Mapper;
import vn.edu.uit.is208.salon.dto.SalonServiceDto;
import vn.edu.uit.is208.salon.entity.SalonService;

@Mapper(componentModel = "spring")
public interface SalonServiceMapper {
    SalonServiceDto toDto(SalonService service);
}
