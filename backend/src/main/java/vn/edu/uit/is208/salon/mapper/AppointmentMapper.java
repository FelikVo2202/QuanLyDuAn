package vn.edu.uit.is208.salon.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.uit.is208.salon.dto.AppointmentDto;
import vn.edu.uit.is208.salon.dto.CreateAppointmentRequest;
import vn.edu.uit.is208.salon.entity.Appointment;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {
    Appointment toEntity(CreateAppointmentRequest request);

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "staffId", source = "staff.id")
    AppointmentDto toDto(Appointment appointment);
}
