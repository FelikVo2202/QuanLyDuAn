package vn.edu.uit.is208.salon.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.uit.is208.salon.entity.Appointment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    @Query("""
                SELECT DISTINCT a
                FROM Appointment a
                JOIN FETCH a.customer
                JOIN FETCH a.staff
                LEFT JOIN FETCH a.services
                WHERE a.appointmentDateTime >= :startDateTime AND a.appointmentDateTime < :endDateTime
                ORDER BY a.appointmentDateTime
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
                AND a.status != 'CANCELED'
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
                ORDER BY a.appointmentDateTime
            """)
    List<Appointment> findAllByStaffIdAndDayRange(
            @Param("staffId") Long staffId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    @Query("""
        SELECT COUNT(DISTINCT a.customer.id)
        FROM Appointment a
        WHERE a.staff.id = :staffId
        AND a.appointmentDateTime >= :startOfMonth AND a.appointmentDateTime < :endOfMonth
        AND (a.status = 'DONE' OR a.status = 'PAID')
    """)
    long countUniqueCustomersByStaffAndMonth(
            @Param("staffId") Long staffId,
            @Param("startOfMonth") LocalDateTime startOfMonth,
            @Param("endOfMonth") LocalDateTime endOfMonth);

    @Query("""
        SELECT COUNT(DISTINCT a.customer.id)
        FROM Appointment a
        WHERE a.appointmentDateTime >= :startDateTime AND a.appointmentDateTime < :endDateTime
        AND a.status != 'CANCELED'
    """)
    long countUniqueCustomersByDayRange(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    @Query("""
        SELECT COUNT(a)
        FROM Appointment a
        WHERE a.staff.id = :staffId
        AND a.appointmentDateTime >= :startOfMonth AND a.appointmentDateTime < :endOfMonth
        AND (a.status = 'DONE' OR a.status = 'PAID')
    """)
    long countCompletedAppointmentsByStaffAndMonth(
            @Param("staffId") Long staffId,
            @Param("startOfMonth") LocalDateTime startOfMonth,
            @Param("endOfMonth") LocalDateTime endOfMonth);

    @EntityGraph(attributePaths = {"services"})
    Optional<Appointment> findById(Long id);

    @Query("""
        SELECT a.staff.firstName, a.staff.lastName, COUNT(s) 
        FROM Appointment a 
        JOIN a.services s 
        WHERE a.appointmentDateTime >= :startOfMonth AND a.appointmentDateTime < :endOfMonth
        AND a.status != 'CANCELED'
        GROUP BY a.staff.firstName, a.staff.lastName 
        ORDER BY COUNT(s) DESC
    """)
    List<Object[]> findTopStaffPerformance(
            @Param("startOfMonth") LocalDateTime startOfMonth,
            @Param("endOfMonth") LocalDateTime endOfMonth,
            org.springframework.data.domain.Pageable pageable);
}