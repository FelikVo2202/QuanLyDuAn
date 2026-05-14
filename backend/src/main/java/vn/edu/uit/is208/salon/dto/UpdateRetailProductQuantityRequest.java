package vn.edu.uit.is208.salon.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRetailProductQuantityRequest {

    @NotNull(message = "Quantity must not be null")
    @Min(value = 1, message = "New quantity must be greater than 0")
    private Long quantity;

}