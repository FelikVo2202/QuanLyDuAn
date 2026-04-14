package vn.edu.uit.is208.salon.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import vn.edu.uit.is208.salon.constant.StaffRole;
import vn.edu.uit.is208.salon.dto.CreateStaffRequest;
import vn.edu.uit.is208.salon.dto.StaffDto;
import vn.edu.uit.is208.salon.service.StaffService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/staffs")
@RequiredArgsConstructor
public class StaffController {
    private final StaffService staffService;

    @PostMapping
    public ResponseEntity<StaffDto> createStaff(@Valid @RequestBody CreateStaffRequest request) {
        StaffDto createdStaff = staffService.createStaff(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdStaff.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdStaff);
    }

    @GetMapping
    public ResponseEntity<List<StaffDto>> getAllStaffs(
            @RequestParam(required = false) StaffRole role) {
        return ResponseEntity.ok(staffService.getAllStaffs(role));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StaffDto> getStaffById(@PathVariable Long id) {
        return ResponseEntity.ok(staffService.getStaffById(id));
    }
}
