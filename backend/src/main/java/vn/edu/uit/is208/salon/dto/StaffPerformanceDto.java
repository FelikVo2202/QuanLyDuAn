package vn.edu.uit.is208.salon.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StaffPerformanceDto {
    private String name;
    private String role;
    private long totalServices;
}
