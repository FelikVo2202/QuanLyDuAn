package vn.edu.uit.is208.salon.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CreateAppointmentRequest {
    @NotNull(message = "Customer must not be null")
    private Long customerId;

    @NotNull(message = "Staff must not be null")
    private Long staffId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "Appointment time must not be null")
    @Future(message = "Appointment time must be in the future")
    private LocalDateTime appointmentDateTime;

    @NotEmpty(message = "Service list must not be empty")
    private Set<Long> serviceIds;
}
