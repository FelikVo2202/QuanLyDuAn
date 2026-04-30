package vn.edu.uit.is208.salon.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import vn.edu.uit.is208.salon.constant.InventoryTransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class InventoryLedgerResponse {
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal changeAmount;
    private InventoryTransactionType transactionType;
    private Long referenceId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transactionDate;
}
