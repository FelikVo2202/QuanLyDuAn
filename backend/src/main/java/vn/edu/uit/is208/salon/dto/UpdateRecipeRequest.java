package vn.edu.uit.is208.salon.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateRecipeRequest {
    @NotNull(message = "Định mức tiêu hao không được để trống")
    @Positive(message = "Định mức tiêu hao phải là số dương")
    private BigDecimal quantityConsumed;
}