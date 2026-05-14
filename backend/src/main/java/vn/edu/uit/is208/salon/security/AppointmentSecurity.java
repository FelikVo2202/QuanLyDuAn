package vn.edu.uit.is208.salon.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.edu.uit.is208.salon.repository.AppointmentRepository;

@Component("appointmentSecurity")
@RequiredArgsConstructor
public class AppointmentSecurity {

    private final AppointmentRepository appointmentRepository;

    public boolean isOwner(Long appointmentId, Long staffId) {
        if (appointmentId == null || staffId == null) return false;

        return appointmentRepository.findById(appointmentId)
                .map(a -> a.getStaff().getId().equals(staffId))
                .orElse(false);
    }
}