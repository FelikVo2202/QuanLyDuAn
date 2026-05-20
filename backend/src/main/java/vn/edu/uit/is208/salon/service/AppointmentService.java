package vn.edu.uit.is208.salon.service;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.edu.uit.is208.salon.constant.AppointmentStatus;
import vn.edu.uit.is208.salon.constant.StaffRole;
import vn.edu.uit.is208.salon.dto.AppointmentDto;
import vn.edu.uit.is208.salon.dto.CreateAppointmentRequest;
import vn.edu.uit.is208.salon.dto.UpdateAppointmentRequest;
import vn.edu.uit.is208.salon.entity.*;
import vn.edu.uit.is208.salon.exception.BusinessRuleException;
import vn.edu.uit.is208.salon.exception.ResourceNotFoundException;
import vn.edu.uit.is208.salon.mapper.AppointmentMapper;
import vn.edu.uit.is208.salon.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentMapper appointmentMapper;
    private final AppointmentRepository appointmentRepository;
    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;
    private final SalonServiceRepository salonServiceRepository;
    private final BillRepository billRepository;
    private final InventoryService inventoryService;
    @Value("${salon.business-hours.open}")
    private LocalTime openingTime;
    @Value("${salon.business-hours.close}")
    private LocalTime closingTime;

    private static void validateDateRange(LocalDate resolvedEndDate, LocalDate resolvedStartDate) {
        if (resolvedEndDate.isBefore(resolvedStartDate)) {
            throw new IllegalArgumentException("End date must not be before start date");
        }
    }

    public List<AppointmentDto> getAllAppointments(LocalDate startDate, LocalDate endDate, Staff staff) {
        LocalDate resolvedStartDate = (startDate != null) ? startDate : LocalDate.now();
        LocalDate resolvedEndDate = (endDate != null) ? endDate : resolvedStartDate;

        validateDateRange(resolvedEndDate, resolvedStartDate);

        LocalDateTime startDateTime = resolvedStartDate.atStartOfDay();
        LocalDateTime endDateTime = resolvedEndDate.plusDays(1).atStartOfDay();

        List<Appointment> appointments;
        if (staff != null && staff.getRole() == StaffRole.STYLIST) {
            appointments = appointmentRepository.findAllByStaffIdAndDayRange(
                    staff.getId(), startDateTime, endDateTime);
        } else {
            appointments = appointmentRepository.findAllByDayRange(startDateTime, endDateTime);
        }

        List<Long> appointmentIds = appointments.stream().map(Appointment::getId).toList();
        Map<Long, Bill> billMap = new java.util.HashMap<>();

        if (!appointmentIds.isEmpty()) {
            List<Bill> activeBills = billRepository.findActiveBillsByAppointmentIds(appointmentIds);
            activeBills.forEach(b -> billMap.put(b.getAppointment().getId(), b));
        }

        return appointments
                .stream()
                .map(appointment -> {
                    AppointmentDto dto = appointmentMapper.toDto(appointment);
                    Bill associatedBill = billMap.get(appointment.getId());
                    if (associatedBill != null) {
                        dto.setBillId(associatedBill.getId());
                        dto.setBillPaymentStatus(associatedBill.getPaymentStatus().name());
                    }
                    return dto;
                })
                .toList();
    }

    public AppointmentDto getAppointmentById(Long id) {
        Appointment appointment = getAppointment(id);
        AppointmentDto dto = appointmentMapper.toDto(appointment);

        billRepository.findActiveBillByAppointmentId(id).ifPresent(bill -> {
            dto.setBillId(bill.getId());
            dto.setBillPaymentStatus(bill.getPaymentStatus().name());
        });

        return dto;
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
        validateAppointmentDateTime(request.getAppointmentDateTime(), appointment.getAppointmentDateTime());
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

    private void validateAppointmentDateTime( LocalDateTime newDateTime, LocalDateTime oldDateTime) {
        if (!newDateTime.isEqual(oldDateTime) && newDateTime.isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("The rescheduled time must be in the future");
        }
    }

    @Transactional
    public AppointmentDto cancelAppointment(Long id) {
        Appointment appointment = getAppointment(id);
        ensureAppointmentIsModifiable(appointment);
        appointment.setStatus(AppointmentStatus.CANCELED);
        return appointmentMapper.toDto(appointment);
    }

    private @NonNull Appointment getAppointment(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));
    }

    private @NonNull Customer getCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
    }

    private @NonNull Staff getStaff(Long staffId) {
        return staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with ID: " + staffId));
    }

    private @NonNull List<SalonService> getSalonServices(Set<Long> serviceIds) {
        List<SalonService> services = salonServiceRepository.findAllById(serviceIds);
        if (services.size() != serviceIds.size()) {
            throw new ResourceNotFoundException("Some of the selected services do not exist");
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
            throw new IllegalStateException("Cannot modify an appointment that is canceled or completed");
        }
    }

    private void validateStylistRole(Staff staff) {
        if (staff.getRole() != StaffRole.STYLIST) {
            throw new BusinessRuleException("The selected staff member must be a stylist");
        }
    }

    private void validateBusinessHours(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        LocalTime startTime = startDateTime.toLocalTime();
        LocalTime endTime = endDateTime.toLocalTime();

        if (startTime.isBefore(openingTime) || startTime.isAfter(closingTime)) {
            throw new BusinessRuleException("Salon accepts appointments from " + openingTime + " to " + closingTime);
        }

        if (endTime.isAfter(closingTime) || !startDateTime.toLocalDate().isEqual(endDateTime.toLocalDate())) {
            throw new BusinessRuleException("The estimated end time (" + endTime + ") exceeds closing time (" + closingTime + ")");
        }
    }

    private void validateStaffAvailability(Long staffId, LocalDateTime startDateTime, LocalDateTime endDateTime, Long appointmentId) {
        boolean isBusy = (appointmentId == null)
                ? appointmentRepository.isStaffBusy(staffId, startDateTime, endDateTime)
                : appointmentRepository.isStaffBusyForUpdate(staffId, startDateTime, endDateTime, appointmentId);

        if (isBusy) {
            throw new BusinessRuleException("Staff " + staffId + " is not available during this time slot");
        }
    }
}
