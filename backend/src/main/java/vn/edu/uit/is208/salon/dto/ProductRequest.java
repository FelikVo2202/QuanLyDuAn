package vn.edu.uit.is208.salon.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import vn.edu.uit.is208.salon.constant.ProductType;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProductRequest {
    @NotBlank(message = "Product name must not be blank")
    @Size(max = 100, message = "Product name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Product type must not be null")
    private ProductType productType;

    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;

    @NotNull(message = "Price must not be null")
    @PositiveOrZero(message = "Price must be greater than or equal to 0")
    private BigDecimal price;

    @NotBlank(message = "Base unit of measure must not be blank")
    @Size(max = 20, message = "Unit of measure must not exceed 20 characters")
    private String baseUom;

    @NotBlank(message = "Purchasing unit of measure must not be blank")
    @Size(max = 20, message = "Unit of measure must not exceed 20 characters")
    private String purchasingUom;

    private BigDecimal conversionFactor;
}