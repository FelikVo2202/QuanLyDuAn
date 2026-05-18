package vn.edu.uit.is208.salon.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ManagerDashboardResponse {
    private BigDecimal revenueToday;
    private BigDecimal revenueThisMonth;
    private long newCustomersThisMonth;
    private List<RevenueHistoryDto> revenueHistory;
    private TodayScheduleDto todaySchedule;
    private List<StaffPerformanceDto> topStaff;

    @Data
    @Builder
    public static class RevenueHistoryDto {
        private String month;
        private BigDecimal revenue;
    }

    @Data
    @Builder
    public static class TodayScheduleDto {
        private long total;
        private long confirmed;
        private long done;
        private long cancelled;
    }

    @Data
    @Builder
    public static class StaffPerformanceDto {
        private String name;
        private String avatar;
        private long servicesCount;
    }
}
