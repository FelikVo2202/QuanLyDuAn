package vn.edu.uit.is208.salon.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateRetailBillRequest {
    @NotNull(message = "customerId must not be null")
    private Long customerId;

    @NotEmpty(message = "retailProducts must not be empty")
    @Valid
    private List<AddRetailProductRequest> retailProducts;
}