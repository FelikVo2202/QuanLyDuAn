package vn.edu.uit.is208.salon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class SalonServiceDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private Long durationMinutes;
}
