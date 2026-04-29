package vn.edu.uit.is208.salon.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.uit.is208.salon.dto.StockUpdateRequest;
import vn.edu.uit.is208.salon.service.InventoryService;

@RestController
@RequestMapping("/api/inventory-ledgers")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @PostMapping("/stock-in")
    public ResponseEntity<?> addStock(@RequestBody @Valid StockUpdateRequest request) {
        inventoryService.addStock(request);
        return ResponseEntity.ok("Nhập kho thành công");
    }
}