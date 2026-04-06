package vn.edu.uit.is208.salon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.edu.uit.is208.salon.dto.AuthResult;
import vn.edu.uit.is208.salon.dto.LoginRequest;
import vn.edu.uit.is208.salon.dto.StaffDto;
import vn.edu.uit.is208.salon.entity.Staff;
import vn.edu.uit.is208.salon.exception.ResourceNotFoundException;
import vn.edu.uit.is208.salon.mapper.StaffMapper;
import vn.edu.uit.is208.salon.repository.StaffRepository;
import vn.edu.uit.is208.salon.security.JwtService;
import vn.edu.uit.is208.salon.security.StaffPrincipal;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final StaffMapper staffMapper;
    private final StaffRepository staffRepository;

    public AuthResult login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        StaffPrincipal staffPrincipal = (StaffPrincipal) Objects.requireNonNull(authentication.getPrincipal());
        Staff staff = staffPrincipal.staff();

        String accessToken = jwtService.generateAccessToken(staff);
        StaffDto staffDto = staffMapper.toDto(staff);

        return new AuthResult(accessToken, staffDto);
    }

    public Staff getCurrentStaff() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AuthenticationCredentialsNotFoundException("Người dùng chưa xác thực hoặc token không hợp lệ");
        }

        var staffId = Long.valueOf(authentication.getPrincipal().toString());

        return staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với ID: " + staffId));
    }
}
