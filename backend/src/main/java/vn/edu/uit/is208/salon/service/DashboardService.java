package vn.edu.uit.is208.salon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.uit.is208.salon.constant.AppointmentStatus;
import vn.edu.uit.is208.salon.dto.ReceptionistDashboardResponse;
import vn.edu.uit.is208.salon.dto.StylistDashboardResponse;
import vn.edu.uit.is208.salon.entity.Appointment;
import vn.edu.uit.is208.salon.entity.SalonService;
import vn.edu.uit.is208.salon.repository.AppointmentRepository;
import vn.edu.uit.is208.salon.repository.SalonServiceRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final AppointmentRepository appointmentRepository;
    private final SalonServiceRepository salonServiceRepository;
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
                .status(appointment.getStatus().name())
                .duration(durationHours)
                .build();
    }

    public ReceptionistDashboardResponse getReceptionistDashboard(Long id) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(LocalTime.MAX);

        List<Appointment> remainingAppointments = appointmentRepository.findAllByDayRange(now, endOfDay)
                .stream()
                .filter(appointment -> appointment.getStatus() != AppointmentStatus.CANCELED)
                .toList();

        long totalRemainingAppointments = remainingAppointments.size();

        long totalCustomersDay = appointmentRepository.countUniqueCustomersByDayRange(startOfDay, endOfDay);

        long totalServices = salonServiceRepository.count();

        List<ReceptionistDashboardResponse.ReceptionistDashboardAppointmentDto> appointmentDtos = remainingAppointments.stream()
                .map(this::mapToReceptionistAppointmentDto)
                .collect(Collectors.toList());

        return ReceptionistDashboardResponse.builder()
                .totalRemainingAppointments(totalRemainingAppointments)
                .totalCustomersDay(totalCustomersDay)
                .totalServices(totalServices)
                .remainingAppointments(appointmentDtos)
                .build();
    }

    private ReceptionistDashboardResponse.ReceptionistDashboardAppointmentDto mapToReceptionistAppointmentDto(Appointment appointment) {
        String serviceNames = appointment.getServices().stream()
                .map(SalonService::getName)
                .collect(Collectors.joining(", "));

        double durationHours = appointment.getServices().stream()
                .mapToLong(SalonService::getDurationMinutes)
                .sum() / 60.0;

        String stylist = appointment.getStaff().getFirstName();

        BigDecimal price = appointment.getServices().stream()
                .map(SalonService::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ReceptionistDashboardResponse.ReceptionistDashboardAppointmentDto.builder()
                .id(appointment.getId())
                .time(appointment.getAppointmentDateTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .status(appointment.getStatus().name())
                .customer(appointment.getCustomer().getFirstName() + " " + appointment.getCustomer().getLastName())
                .service(serviceNames)
                .duration(durationHours)
                .stylist(stylist)
                .price(price)
                .build();
    }
}
