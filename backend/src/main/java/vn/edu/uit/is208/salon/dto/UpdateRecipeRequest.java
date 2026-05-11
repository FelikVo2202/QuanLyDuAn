package vn.edu.uit.is208.salon.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateRecipeRequest {
    @NotNull(message = "Consumption rate must not be null")
    @Positive(message = "Consumption rate must be a positive number")
    private BigDecimal quantityConsumed;
}