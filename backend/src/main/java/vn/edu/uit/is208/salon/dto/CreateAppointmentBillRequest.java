package vn.edu.uit.is208.salon.dto;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class CreateAppointmentBillRequest {
    @Valid
    private List<AddRetailProductRequest> retailProducts;
}
