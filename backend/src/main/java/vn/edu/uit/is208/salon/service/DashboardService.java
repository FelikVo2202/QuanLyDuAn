package vn.edu.uit.is208.salon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.uit.is208.salon.dto.StylistDashboardResponse;
import vn.edu.uit.is208.salon.entity.Appointment;
import vn.edu.uit.is208.salon.entity.SalonService;
import vn.edu.uit.is208.salon.repository.AppointmentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final AppointmentRepository appointmentRepository;
    private static final long MONTHLY_TARGET = 120;

    public StylistDashboardResponse getStylistDashboard(Long staffId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(LocalTime.MAX);
        
        LocalDateTime startOfMonth = now.toLocalDate().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = now.toLocalDate().plusMonths(1).withDayOfMonth(1).atStartOfDay();

        // 1. Today's appointments
        List<Appointment> todayAppointments = appointmentRepository.findAllByStaffIdAndDayRange(
                staffId, startOfDay, endOfDay);

        // 2. Remaining appointments today (time > now)
        long remainingToday = todayAppointments.stream()
                .filter(a -> a.getAppointmentDateTime().isAfter(now))
                .count();

        // 3. Total unique customers this month
        long totalCustomersMonth = appointmentRepository.countUniqueCustomersByStaffAndMonth(
                staffId, startOfMonth, endOfMonth);

        // 4. Completed appointments this month
        long completedMonth = appointmentRepository.countCompletedAppointmentsByStaffAndMonth(
                staffId, startOfMonth, endOfMonth);

        // Map today's appointments to DTO
        List<StylistDashboardResponse.StylistDashboardAppointmentDto> appointmentDtos = todayAppointments.stream()
                .map(this::mapToStylistAppointmentDto)
                .collect(Collectors.toList());

        return StylistDashboardResponse.builder()
                .totalCustomersMonth(totalCustomersMonth)
                .remainingAppointmentsToday(remainingToday)
                .completedAppointmentsMonth(completedMonth)
                .todayAppointments(appointmentDtos)
                .monthlyTarget(MONTHLY_TARGET)
                .build();
    }

    private StylistDashboardResponse.StylistDashboardAppointmentDto mapToStylistAppointmentDto(Appointment appointment) {
        String serviceNames = appointment.getServices().stream()
                .map(SalonService::getName)
                .collect(Collectors.joining(", "));

        double durationHours = appointment.getServices().stream()
                .mapToLong(SalonService::getDurationMinutes)
                .sum() / 60.0;

        return StylistDashboardResponse.StylistDashboardAppointmentDto.builder()
                .id(appointment.getId())
                .customer(appointment.getCustomer().getFirstName() + " " + appointment.getCustomer().getLastName())
                .service(serviceNames)
                .time(appointment.getAppointmentDateTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .status(appointment.getStatus().name().toLowerCase())
                .duration(durationHours)
                .build();
    }
}
