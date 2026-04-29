package vn.edu.uit.is208.salon.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import vn.edu.uit.is208.salon.constant.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class BillDto {
    private Long id;
    private Long appointmentId;
    private Long customerId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime billDate;

    private BigDecimal totalAmount;
    private PaymentStatus paymentStatus;
    private List<BillDetailDto> details;
}
