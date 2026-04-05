package vn.edu.uit.is208.salon.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import vn.edu.uit.is208.salon.constant.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.Set;

@AllArgsConstructor
@Getter
public class AppointmentDto {
    private Long id;

    private Long customerId;

    private Long staffId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appointmentDateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDateTime;

    private AppointmentStatus status;

    private Set<SalonServiceDto> services;
}
