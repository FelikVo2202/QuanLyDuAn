package vn.edu.uit.is208.salon.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class ServiceRecipeId implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull
    @Column(name = "SERVICEID", nullable = false)
    private Long serviceId;

    @NotNull
    @Column(name = "PRODUCTID", nullable = false)
    private Long productId;
}