package vn.edu.uit.is208.salon.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.uit.is208.salon.dto.CreateStaffRequest;
import vn.edu.uit.is208.salon.dto.StaffDto;
import vn.edu.uit.is208.salon.entity.Staff;
import vn.edu.uit.is208.salon.exception.DuplicateResourceException;
import vn.edu.uit.is208.salon.mapper.StaffMapper;
import vn.edu.uit.is208.salon.repository.StaffRepository;

@Service
@RequiredArgsConstructor
public class StaffService {
    private final StaffRepository staffRepository;
    private final StaffMapper staffMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public StaffDto createStaff(CreateStaffRequest request) {
        validateUsernameUniqueness(request.getUsername());

        Staff staff = staffMapper.toEntity(request);
        staff.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        return staffMapper.toDto(staffRepository.save(staff));

    }

    private void validateUsernameUniqueness(String username) {
        if (staffRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Tên đăng nhập '" + username + "' đã tồn tại!");
        }
    }
}
