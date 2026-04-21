package vn.edu.uit.is208.salon.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "SERVICE_RECIPE")
public class ServiceRecipe {
    @EmbeddedId
    private ServiceRecipeId id = new ServiceRecipeId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("serviceId")
    @JoinColumn(name = "SERVICEID", nullable = false)
    private SalonService service;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("productId")
    @JoinColumn(name = "PRODUCTID", nullable = false)
    private Product product;

    @NotNull
    @Column(name = "QUANTITYCONSUMED", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantityConsumed;
}
