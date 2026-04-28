package vn.edu.uit.is208.salon.projection;

import vn.edu.uit.is208.salon.constant.ProductType;

import java.math.BigDecimal;

public interface ProductSummary {
    Long getId();
    BigDecimal getPrice();
    BigDecimal getConversionFactor();
    ProductType getProductType();
}
