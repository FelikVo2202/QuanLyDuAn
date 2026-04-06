package vn.edu.uit.is208.salon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResult {
    private String accessToken;
    private String refreshToken;
    private StaffDto staff;
}
