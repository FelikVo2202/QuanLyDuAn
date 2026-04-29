package vn.edu.uit.is208.salon.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddRetailProductRequest {
    @NotNull(message = "productId không được để trống")
    private Long productId;

    @NotNull(message = "quantity không được để trống")
    @Min(value = 1, message = "Số lượng phải từ 1 trở lên")
    private Long quantity;
}
