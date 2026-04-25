package vn.edu.uit.is208.salon.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateBillRequest {
    private Long appointmentId;

    @NotNull(message = "Khách hàng không được để trống")
    private Long customerId;

    @Valid
    @NotEmpty(message = "Hóa đơn phải có ít nhất 1 dịch vụ/sản phẩm")
    private List<CreateBillDetailRequest> details;
}
