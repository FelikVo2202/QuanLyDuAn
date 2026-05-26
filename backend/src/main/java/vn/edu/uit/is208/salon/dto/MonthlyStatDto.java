package vn.edu.uit.is208.salon.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MonthlyStatDto {
    private String month;
    private long count;
}
