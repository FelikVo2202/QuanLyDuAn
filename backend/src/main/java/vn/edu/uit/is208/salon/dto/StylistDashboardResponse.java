package vn.edu.uit.is208.salon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StylistDashboardResponse {
    private Long totalCustomersMonth;
    private Long remainingAppointmentsToday;
    private Long completedAppointmentsMonth;
    private List<StylistDashboardAppointmentDto> todayAppointments;
    private Long monthlyTarget;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StylistDashboardAppointmentDto {
        private Long id;
        private String customer;
        private String service;
        private String time;
        private String status;
        private Double duration;
    }
}
