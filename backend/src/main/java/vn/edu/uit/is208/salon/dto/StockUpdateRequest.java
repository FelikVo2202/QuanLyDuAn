package vn.edu.uit.is208.salon.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockUpdateRequest {
    @NotNull(message = "Product ID must not be null")
    private Long productId;

    @NotNull(message = "Quantity must not be null")
    private BigDecimal quantity;

    private Long referenceId;
}