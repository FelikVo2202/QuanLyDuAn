package vn.edu.uit.is208.salon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import vn.edu.uit.is208.salon.constant.StaffRole;

@AllArgsConstructor
@Getter
public class StaffDto {
    private Long id;
    private String firstName;
    private String lastName;
    private StaffRole role;
    private String username;
}
