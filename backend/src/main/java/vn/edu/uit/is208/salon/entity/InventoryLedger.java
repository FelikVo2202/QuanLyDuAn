package vn.edu.uit.is208.salon.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.edu.uit.is208.salon.constant.InventoryTransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "INVENTORY_LEDGER")
public class InventoryLedger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TRANSACTIONID", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PRODUCTID", nullable = false)
    private Product product;

    @NotNull
    @Column(name = "CHANGEAMOUNT", nullable = false, precision = 10, scale = 2)
    private BigDecimal changeAmount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "TRANSACTIONTYPE", nullable = false, length = 50)
    private InventoryTransactionType transactionType;

    @Column(name = "REFERENCEID")
    private Long referenceId;

    @Column(name = "TRANSACTIONDATE")
    private LocalDateTime transactionDate = LocalDateTime.now();
}
