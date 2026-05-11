package vn.edu.uit.is208.salon.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateRecipeRequest {
    @NotNull(message = "Product ID must not be null")
    private Long productId;

    @NotNull(message = "Consumption quantity must not be null")
    @Positive(message = "Consumption quantity must be greater than 0")
    private BigDecimal quantityConsumed;
}