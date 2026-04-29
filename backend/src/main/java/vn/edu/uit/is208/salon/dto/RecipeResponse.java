package vn.edu.uit.is208.salon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeResponse {
    private Long serviceId;
    private String serviceName;
    private Long productId;
    private String productName;
    private BigDecimal quantityConsumed;
    private String baseUom;
}