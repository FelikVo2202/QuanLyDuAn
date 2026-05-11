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
public class UpdateAppointmentRequest {
    @NotNull(message = "Nhân viên không được trống")
    private Long staffId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "Khung giờ không được trống")
//    @Future(message = "Thời gian hẹn phải là một thời điểm trong tương lai")
    private LocalDateTime appointmentDateTime;

    @NotEmpty(message = "Dịch vụ không được trống")
    private Set<Long> serviceIds;
}
