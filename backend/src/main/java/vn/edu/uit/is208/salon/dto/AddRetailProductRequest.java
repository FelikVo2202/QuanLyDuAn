package vn.edu.uit.is208.salon.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddRetailProductRequest {
    @NotNull(message = "productId must not be null")
    private Long productId;

    @NotNull(message = "quantity must not be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Long quantity;
}
