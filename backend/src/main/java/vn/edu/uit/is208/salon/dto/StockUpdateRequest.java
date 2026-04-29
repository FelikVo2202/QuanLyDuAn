package vn.edu.uit.is208.salon.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StockUpdateRequest {
    private Long productId;
    private BigDecimal quantity;
}