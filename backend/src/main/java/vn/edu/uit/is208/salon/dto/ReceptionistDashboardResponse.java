package vn.edu.uit.is208.salon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceptionistDashboardResponse {
    private Long totalRemainingAppointments;
    private Long totalCustomersDay;
    private Long totalServices;
    private List<ReceptionistDashboardAppointmentDto> remainingAppointments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceptionistDashboardAppointmentDto {
        private Long id;
        private String time;
        private String status;
        private String customer;
        private String service;
        private Double duration;
        private String stylist;
        private BigDecimal price;
    }
}
