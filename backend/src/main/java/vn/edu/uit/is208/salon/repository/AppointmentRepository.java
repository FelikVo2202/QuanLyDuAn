package vn.edu.uit.is208.salon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.uit.is208.salon.entity.Appointment;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    @Query("""
                SELECT DISTINCT a
                FROM Appointment a
                JOIN FETCH a.customer
                JOIN FETCH a.staff
                LEFT JOIN FETCH a.services
                WHERE a.appointmentDateTime >= :startDateTime AND a.appointmentDateTime < :endDateTime
            """)
    List<Appointment> findAllByDayRange(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    @Query("""
                SELECT COUNT(a) > 0
                FROM Appointment a
                WHERE a.staff.id = :staffId
                AND a.appointmentDateTime <= :endDateTime AND a.endDateTime >= :startDateTime
                AND a.status != 'CANCELED'
            """)
    boolean isStaffBusy(
            @Param("staffId") Long staffId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    @Query("""
                SELECT COUNT(a) > 0
                FROM Appointment a
                WHERE a.staff.id = :staffId
                AND a.id != :excludeAppointmentId
                AND a.appointmentDateTime < :endDateTime AND a.endDateTime > :startDateTime
                AND a.status != 'Canceled'
            """)
    boolean isStaffBusyForUpdate(
            @Param("staffId") Long staffId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("excludeAppointmentId") Long excludeAppointmentId);

    @Query("""
                SELECT DISTINCT a
                FROM Appointment a
                JOIN FETCH a.customer
                JOIN FETCH a.staff
                LEFT JOIN FETCH a.services
                WHERE a.staff.id = :staffId
                AND a.appointmentDateTime >= :startDateTime AND a.appointmentDateTime < :endDateTime
            """)
    List<Appointment> findAllByStaffIdAndDayRange(
            @Param("staffId") Long staffId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);
}