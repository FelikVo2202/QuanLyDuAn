package vn.edu.uit.is208.salon.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RecipeItemResponse {
    private Long productId;
    private String productName;
    private BigDecimal quantityConsumed;
    private String baseUom;
}