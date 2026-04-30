package vn.edu.uit.is208.salon.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import vn.edu.uit.is208.salon.constant.ProductType;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProductRequest {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    @NotNull(message = "Loại sản phẩm không được để trống")
    private ProductType productType;

    private String category;

    @NotNull(message = "Giá không được để trống")
    @Positive(message = "Giá phải lớn hơn 0")
    private BigDecimal price;

    @NotBlank(message = "Đơn vị tính cơ bản không được để trống")
    private String baseUom;

    private String purchasingUom;
    private BigDecimal conversionFactor;
}