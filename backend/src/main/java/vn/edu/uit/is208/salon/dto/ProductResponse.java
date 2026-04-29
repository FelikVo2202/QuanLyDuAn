package vn.edu.uit.is208.salon.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String productType;
    private String category;
    private BigDecimal price;
    private String baseUom;
    private BigDecimal quantityOnHand;
}