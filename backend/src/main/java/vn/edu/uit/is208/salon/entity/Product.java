package vn.edu.uit.is208.salon.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.SQLRestriction;
import vn.edu.uit.is208.salon.constant.ProductType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "PRODUCT")
@DynamicUpdate
@SQLRestriction("DELETEDAT IS NULL")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PRODUCTID", nullable = false)
    private Long id;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "PRODUCTTYPE", nullable = false, length = 20)
    private ProductType productType = ProductType.BOTH;

    @Size(max = 50)
    @Nationalized
    @Column(name = "CATEGORY", length = 50)
    private String category;

    @NotNull
    @Column(name = "PRICE", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Size(max = 20)
    @NotNull
    @Column(name = "BASEUOM", nullable = false, length = 20)
    private String baseUom;

    @Size(max = 20)
    @NotNull
    @Column(name = "PURCHASINGUOM", nullable = false, length = 20)
    private String purchasingUom;

    @Column(name = "CONVERSIONFACTOR", precision = 10, scale = 2)
    private BigDecimal conversionFactor;

    @NotNull
    @Column(name = "QUANTITYONHAND", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantityOnHand = BigDecimal.ZERO;

    @Column(name = "DELETEDAT")
    private LocalDateTime deletedAt = null;
}
