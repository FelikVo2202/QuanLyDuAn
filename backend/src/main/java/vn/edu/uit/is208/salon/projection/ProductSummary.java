package vn.edu.uit.is208.salon.projection;

import java.math.BigDecimal;

public interface ProductSummary {
    Long getId();
    BigDecimal getPrice();
    BigDecimal getConversionFactor();
}
