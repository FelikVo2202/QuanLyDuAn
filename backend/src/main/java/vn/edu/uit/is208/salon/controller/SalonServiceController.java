package vn.edu.uit.is208.salon.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.uit.is208.salon.dto.SalonServiceDto;
import vn.edu.uit.is208.salon.service.SalonServiceService;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class SalonServiceController {
    private final SalonServiceService salonServiceService;

    @GetMapping
    public ResponseEntity<List<SalonServiceDto>> getAllServices() {
        return ResponseEntity.ok(salonServiceService.getAllServices());
    }
}
