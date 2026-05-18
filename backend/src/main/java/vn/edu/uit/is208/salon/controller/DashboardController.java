package vn.edu.uit.is208.salon.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.uit.is208.salon.dto.ReceptionistDashboardResponse;
import vn.edu.uit.is208.salon.dto.StylistDashboardResponse;
import vn.edu.uit.is208.salon.security.StaffPrincipal;
import vn.edu.uit.is208.salon.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/stylist")
    @PreAuthorize("hasRole('STYLIST')")
    public ResponseEntity<StylistDashboardResponse> getStylistDashboard(@AuthenticationPrincipal StaffPrincipal principal) {
        return ResponseEntity.ok(dashboardService.getStylistDashboard(principal.staff().getId()));
    }

    @GetMapping("/receptionist")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public ResponseEntity<ReceptionistDashboardResponse> getReceptionistDashboard(@AuthenticationPrincipal StaffPrincipal principal) {
        return ResponseEntity.ok(dashboardService.getReceptionistDashboard(principal.staff().getId()));
    }
}
