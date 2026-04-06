package vn.edu.uit.is208.salon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import vn.edu.uit.is208.salon.dto.AuthResult;
import vn.edu.uit.is208.salon.dto.LoginRequest;
import vn.edu.uit.is208.salon.dto.StaffDto;
import vn.edu.uit.is208.salon.entity.Staff;
import vn.edu.uit.is208.salon.mapper.StaffMapper;
import vn.edu.uit.is208.salon.security.JwtService;
import vn.edu.uit.is208.salon.security.StaffPrincipal;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final StaffMapper staffMapper;

    public AuthResult login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        StaffPrincipal staffPrincipal = (StaffPrincipal) Objects.requireNonNull(authentication.getPrincipal());
        Staff staff = staffPrincipal.staff();

        String accessToken = jwtService.generateAccessToken(staff);
        StaffDto staffDto = staffMapper.toDto(staff);

        return new AuthResult(accessToken, staffDto);
    }
}
