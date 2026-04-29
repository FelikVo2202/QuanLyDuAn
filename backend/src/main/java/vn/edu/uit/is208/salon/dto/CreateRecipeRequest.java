package vn.edu.uit.is208.salon.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class CreateRecipeRequest {
    @NotNull(message = "ID dịch vụ không được để trống")
    private Long serviceId;

    @NotNull(message = "ID sản phẩm không được để trống")
    private Long productId;

    @NotNull(message = "Lượng tiêu hao không được để trống")
    @Positive(message = "Lượng tiêu hao phải lớn hơn 0")
    private Double amountNeeded;
}