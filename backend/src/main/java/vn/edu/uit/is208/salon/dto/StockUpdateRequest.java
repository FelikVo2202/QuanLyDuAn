package vn.edu.uit.is208.salon.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockUpdateRequest {
    @NotNull(message = "Mã sản phẩm không được trống")
    private Long productId;

    @NotNull(message = "Số lượng không được trống")
    private BigDecimal quantity;

    private Long referenceId;
}