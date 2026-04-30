package vn.edu.uit.is208.salon.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import vn.edu.uit.is208.salon.constant.ProductType;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProductRequest {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 100, message = "Tên sản phẩm không được vượt quá 100 ký tự")
    private String name;

    @NotNull(message = "Loại sản phẩm không được để trống")
    private ProductType productType;

    @Size(max = 50, message = "Danh mục không được vượt quá 50 ký tự")
    private String category;

    @NotNull(message = "Giá không được để trống")
    @PositiveOrZero(message = "Giá bán không được nhỏ hơn 0")
    private BigDecimal price;

    @NotBlank(message = "Đơn vị tính cơ bản không được để trống")
    @Size(max = 20, message = "Đơn vị tính không được vượt quá 20 ký tự")
    private String baseUom;

    @NotBlank(message = "Đơn vị tính mua hàng không được để trống")
    @Size(max = 20, message = "Đơn vị tính không được vượt quá 20 ký tự")
    private String purchasingUom;

    private BigDecimal conversionFactor;
}