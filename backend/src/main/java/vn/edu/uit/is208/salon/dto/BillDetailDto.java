package vn.edu.uit.is208.salon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class BillDetailDto {
    private Long id;
    private Long serviceId;
    private Long productId;
    private Long quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}
