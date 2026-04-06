package vn.edu.uit.is208.salon.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.uit.is208.salon.dto.*;
import vn.edu.uit.is208.salon.entity.Staff;
import vn.edu.uit.is208.salon.mapper.StaffMapper;
import vn.edu.uit.is208.salon.security.JwtConfig;
import vn.edu.uit.is208.salon.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final StaffMapper staffMapper;
    private final JwtConfig jwtConfig;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResult authResult = authService.login(request);

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", authResult.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/api/auth/refresh")
                .maxAge(jwtConfig.getRefreshTokenExpiration())
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(new AuthResponse(authResult.getAccessToken(), authResult.getStaff()));
    }

    @GetMapping("/me")
    public ResponseEntity<StaffDto> me() {
        Staff staff = authService.getCurrentStaff();
        return ResponseEntity.ok(staffMapper.toDto(staff));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        return ResponseEntity.ok(new TokenResponse(authService.refreshAccessToken(refreshToken)));
    }

}
