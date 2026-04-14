package vn.edu.uit.is208.salon.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.uit.is208.salon.constant.StaffRole;
import vn.edu.uit.is208.salon.dto.CreateStaffRequest;
import vn.edu.uit.is208.salon.dto.StaffDto;
import vn.edu.uit.is208.salon.entity.Staff;
import vn.edu.uit.is208.salon.exception.DuplicateResourceException;
import vn.edu.uit.is208.salon.exception.ResourceNotFoundException;
import vn.edu.uit.is208.salon.mapper.StaffMapper;
import vn.edu.uit.is208.salon.repository.StaffRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffService {
    private final StaffRepository staffRepository;
    private final StaffMapper staffMapper;
    private final PasswordEncoder passwordEncoder;

    public List<StaffDto> getAllStaffs(StaffRole role) {
        if (role != null) {
            return staffRepository.findByRole(role)
                    .stream()
                    .map(staffMapper::toDto)
                    .toList();
        }
        return staffRepository.findAll()
                .stream()
                .map(staffMapper::toDto)
                .toList();
    }

    public StaffDto getStaffById(Long id) {
        Staff staff = getStaff(id);
        return staffMapper.toDto(staff);
    }

    @Transactional
    public StaffDto createStaff(CreateStaffRequest request) {
        validateUsernameUniqueness(request.getUsername());

        Staff staff = staffMapper.toEntity(request);
        staff.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        return staffMapper.toDto(staffRepository.save(staff));

    }

    private void validateUsernameUniqueness(String username) {
        if (staffRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Tên đăng nhập '" + username + "' đã tồn tại");
        }
    }

    private @NonNull Staff getStaff(Long staffId) {
        return staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với ID: " + staffId));
    }
}
