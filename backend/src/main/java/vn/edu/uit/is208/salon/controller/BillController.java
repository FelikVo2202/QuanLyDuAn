package vn.edu.uit.is208.salon.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import vn.edu.uit.is208.salon.dto.BillDto;
import vn.edu.uit.is208.salon.dto.CreateAppointmentBillRequest;
import vn.edu.uit.is208.salon.dto.CreateRetailBillRequest;
import vn.edu.uit.is208.salon.service.BillService;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BillController {
    private final BillService billService;

    @GetMapping("/bills")
    public ResponseEntity<Page<BillDto>> getAllBills(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(billService.getAllBills(page, size, paymentStatus, customerId, startDate, endDate));
    }

    @GetMapping("/bills/{id}")
    public ResponseEntity<BillDto> getBillById(@PathVariable Long id) {
        return ResponseEntity.ok(billService.getBillById(id));
    }

    @PostMapping("/appointments/{appointmentId}/bills")
    public ResponseEntity<BillDto> createAppointmentBill(
            @PathVariable Long appointmentId, @Valid @RequestBody CreateAppointmentBillRequest request) {

        BillDto createdBill = billService.createAppointmentBill(appointmentId, request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/bills/{id}")
                .buildAndExpand(createdBill.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdBill);
    }

    @PostMapping("/bills/retail")
    public ResponseEntity<BillDto> createRetailBill(@Valid @RequestBody CreateRetailBillRequest request) {
        BillDto createdBill = billService.createRetailBill(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/bills/{id}")
                .buildAndExpand(createdBill.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdBill);
    }
}
