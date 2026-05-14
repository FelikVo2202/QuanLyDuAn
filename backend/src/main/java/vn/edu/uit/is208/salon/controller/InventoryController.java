package vn.edu.uit.is208.salon.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import vn.edu.uit.is208.salon.constant.InventoryTransactionType;
import vn.edu.uit.is208.salon.dto.InventoryLedgerResponse;
import vn.edu.uit.is208.salon.dto.StockUpdateRequest;
import vn.edu.uit.is208.salon.service.InventoryService;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/inventory-ledgers")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @PostMapping("/adjust")
    public ResponseEntity<InventoryLedgerResponse> adjustStock(@RequestBody @Valid StockUpdateRequest request) {
        InventoryLedgerResponse createdInventory = inventoryService.adjustStock(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/inventory-ledgers/{id}")
                .buildAndExpand(createdInventory.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdInventory);
    }

    @GetMapping
    public ResponseEntity<Page<InventoryLedgerResponse>> getAllInventoryLedgers(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) InventoryTransactionType transactionType,
            @RequestParam(required = false) Long referenceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "transactionDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(inventoryService.getAll(
                productId, transactionType, referenceId, startDate, endDate, pageable));
    }
}