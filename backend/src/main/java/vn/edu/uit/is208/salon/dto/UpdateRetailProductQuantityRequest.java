package vn.edu.uit.is208.salon.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRetailProductQuantityRequest {

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng mới phải lớn hơn 0")
    private Long quantity;

}