package vn.edu.uit.is208.salon.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateRecipeRequest {
    @NotNull(message = "ID sản phẩm không được để trống")
    private Long productId;

    @NotNull(message = "Lượng tiêu hao không được để trống")
    @Positive(message = "Lượng tiêu hao phải lớn hơn 0")
    private BigDecimal quantityConsumed;
}