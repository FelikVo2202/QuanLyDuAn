package vn.edu.uit.is208.salon.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookingStatusStatDto {
    private long upcoming;
    private long inProgress;
    private long cancelled;
    private long paid;
}
