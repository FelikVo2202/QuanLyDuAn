package vn.edu.uit.is208.salon.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DashboardSummaryDto {
    private long totalAppointmentsToday;
    private long totalNewCustomersToday;
    private double totalRevenueToday;
    private long pendingAppointments;

    private List<MonthlyStatDto> monthlyStats;
    private BookingStatusStatDto bookingStats;
    private List<StaffPerformanceDto> topPerformers;
}
