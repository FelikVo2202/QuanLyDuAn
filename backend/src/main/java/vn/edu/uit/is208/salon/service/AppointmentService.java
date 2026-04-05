package vn.edu.uit.is208.salon.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.edu.uit.is208.salon.constant.AppointmentStatus;
import vn.edu.uit.is208.salon.constant.StaffRole;
import vn.edu.uit.is208.salon.dto.AppointmentDto;
import vn.edu.uit.is208.salon.dto.CreateAppointmentRequest;
import vn.edu.uit.is208.salon.dto.UpdateAppointmentRequest;
import vn.edu.uit.is208.salon.entity.Appointment;
import vn.edu.uit.is208.salon.entity.Customer;
import vn.edu.uit.is208.salon.entity.SalonService;
import vn.edu.uit.is208.salon.entity.Staff;
import vn.edu.uit.is208.salon.exception.BusinessRuleException;
import vn.edu.uit.is208.salon.exception.ResourceNotFoundException;
import vn.edu.uit.is208.salon.mapper.AppointmentMapper;
import vn.edu.uit.is208.salon.repository.AppointmentRepository;
import vn.edu.uit.is208.salon.repository.CustomerRepository;
import vn.edu.uit.is208.salon.repository.SalonServiceRepository;
import vn.edu.uit.is208.salon.repository.StaffRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentMapper appointmentMapper;
    private final AppointmentRepository appointmentRepository;
    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;
    private final SalonServiceRepository salonServiceRepository;
    @Value("${salon.business-hours.open}")
    private LocalTime openingTime;
    @Value("${salon.business-hours.close}")
    private LocalTime closingTime;

    private static void validateDateRange(LocalDate resolvedEndDate, LocalDate resolvedStartDate) {
        if (resolvedEndDate.isBefore(resolvedStartDate)) {
            throw new IllegalArgumentException("Ngày kết thúc không được nhỏ hơn ngày bắt đầu!");
        }
    }

    public List<AppointmentDto> getAllAppointments(LocalDate startDate, LocalDate endDate) {
        LocalDate resolvedStartDate = (startDate != null) ? startDate : LocalDate.now();
        LocalDate resolvedEndDate = (endDate != null) ? endDate : resolvedStartDate;

        validateDateRange(resolvedEndDate, resolvedStartDate);

        LocalDateTime startDateTime = resolvedStartDate.atStartOfDay();
        LocalDateTime endDateTime = resolvedEndDate.plusDays(1).atStartOfDay();

        return appointmentRepository.findAllByDayRange(startDateTime, endDateTime)
                .stream()
                .map(appointmentMapper::toDto)
                .toList();
    }

    public AppointmentDto getAppointmentById(Long id) {
        Appointment appointment = getAppointment(id);
        return appointmentMapper.toDto(appointment);
    }

    @Transactional
    public AppointmentDto createAppointment(CreateAppointmentRequest request) {
        Customer customer = getCustomer(request.getCustomerId());
        Staff staff = getStaff(request.getStaffId());
        validateStylistRole(staff);
        List<SalonService> services = getSalonServices(request.getServiceIds());
        LocalDateTime startDateTime = request.getAppointmentDateTime();
        LocalDateTime endDateTime = calculateEndDateTime(startDateTime, services);
        validateBusinessHours(startDateTime, endDateTime);
        validateStaffAvailability(staff.getId(), startDateTime, endDateTime, null);

        Appointment appointment = appointmentMapper.toEntity(request);
        appointment.setCustomer(customer);
        appointment.setStaff(staff);
        appointment.setServices(new LinkedHashSet<>(services));
        appointment.setEndDateTime(endDateTime);
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        return appointmentMapper.toDto(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentDto updateAppointment(Long id, UpdateAppointmentRequest request) {
        Appointment appointment = getAppointment(id);
        ensureAppointmentIsModifiable(appointment);
        Staff staff = getStaff(request.getStaffId());
        validateStylistRole(staff);
        List<SalonService> services = getSalonServices(request.getServiceIds());
        LocalDateTime startDateTime = request.getAppointmentDateTime();
        LocalDateTime endDateTime = calculateEndDateTime(startDateTime, services);
        validateBusinessHours(startDateTime, endDateTime);
        validateStaffAvailability(staff.getId(), startDateTime, endDateTime, appointment.getId());

        appointment.setStaff(staff);
        appointment.getServices().clear();
        appointment.getServices().addAll(services);
        appointment.setAppointmentDateTime(startDateTime);
        appointment.setEndDateTime(endDateTime);

        return appointmentMapper.toDto(appointment);
    }

    @Transactional
    public AppointmentDto cancelAppointment(Long id) {
        Appointment appointment = getAppointment(id);
        appointment.setStatus(AppointmentStatus.CANCELED);
        return appointmentMapper.toDto(appointment);
    }

    private @NonNull Appointment getAppointment(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn với ID: " + appointmentId));
    }

    private @NonNull Customer getCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng với ID: " + customerId));
    }

    private @NonNull Staff getStaff(Long staffId) {
        return staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với ID: " + staffId));
    }

    private @NonNull List<SalonService> getSalonServices(Set<Long> serviceIds) {
        List<SalonService> services = salonServiceRepository.findAllById(serviceIds);
        if (services.size() != serviceIds.size()) {
            throw new ResourceNotFoundException("Một số dịch vụ bạn chọn không tồn tại!");
        }
        return services;
    }

    private @NonNull LocalDateTime calculateEndDateTime(LocalDateTime startDateTime, List<SalonService> services) {
        long totalDuration = services.stream()
                .mapToLong(SalonService::getDurationMinutes)
                .sum();
        return startDateTime.plusMinutes(totalDuration);
    }

    private void ensureAppointmentIsModifiable(Appointment appointment) {
        AppointmentStatus status = appointment.getStatus();
        if (status == AppointmentStatus.CANCELED || status == AppointmentStatus.DONE) {
            throw new IllegalStateException("Không thể chỉnh sửa lịch hẹn đã hủy hoặc đã hoàn thành!");
        }
    }

    private void validateStylistRole(Staff staff) {
        if (staff.getRole() != StaffRole.STYLIST) {
            throw new BusinessRuleException("Nhân viên được chọn phải là thợ cắt tóc!");
        }
    }

    private void validateBusinessHours(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        LocalTime startTime = startDateTime.toLocalTime();
        LocalTime endTime = endDateTime.toLocalTime();

        if (startTime.isBefore(openingTime) || startTime.isAfter(closingTime)) {
            throw new BusinessRuleException("Salon chỉ nhận khách từ " + openingTime + " đến " + closingTime + ".");
        }

        if (endTime.isAfter(closingTime) || !startDateTime.toLocalDate().isEqual(endDateTime.toLocalDate())) {
            throw new BusinessRuleException("Thời gian dự kiến kết thúc (" + endTime + ") vượt quá giờ đóng cửa (" + closingTime + ").");
        }
    }

    private void validateStaffAvailability(Long staffId, LocalDateTime startDateTime, LocalDateTime endDateTime, Long appointmentId) {
        boolean isBusy = (appointmentId == null)
                ? appointmentRepository.isStaffBusy(staffId, startDateTime, endDateTime)
                : appointmentRepository.isStaffBusyForUpdate(staffId, startDateTime, endDateTime, appointmentId);

        if (isBusy) {
            throw new BusinessRuleException("Nhân viên " + staffId + " đã có lịch trong khung giờ này!");
        }
    }
}
