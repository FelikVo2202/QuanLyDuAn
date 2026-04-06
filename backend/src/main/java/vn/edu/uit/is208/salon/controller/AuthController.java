package vn.edu.uit.is208.salon.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.uit.is208.salon.dto.AuthResponse;
import vn.edu.uit.is208.salon.dto.AuthResult;
import vn.edu.uit.is208.salon.dto.LoginRequest;
import vn.edu.uit.is208.salon.dto.StaffDto;
import vn.edu.uit.is208.salon.entity.Staff;
import vn.edu.uit.is208.salon.mapper.StaffMapper;
import vn.edu.uit.is208.salon.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final StaffMapper staffMapper;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResult authResult = authService.login(request);
        return ResponseEntity.ok(new AuthResponse(authResult.getAccessToken(), authResult.getStaff()));
    }

    @GetMapping("/me")
    public ResponseEntity<StaffDto> me() {
        Staff staff = authService.getCurrentStaff();
        return ResponseEntity.ok(staffMapper.toDto(staff));
    }
}
