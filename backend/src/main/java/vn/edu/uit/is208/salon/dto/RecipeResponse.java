package vn.edu.uit.is208.salon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeResponse {
    private Long id;
    private Long serviceId;
    private String serviceName;
    private Long productId;
    private String productName;
    private Double amountNeeded;
    private String baseUom;
}