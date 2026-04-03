package vn.edu.uit.is208.salon.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "APPOINTMENT")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "APPOINTMENTID", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "CUSTOMERID", nullable = false)
    private Customer customer;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "STAFFID", nullable = false)
    private Staff staff;

    @NotNull
    @Column(name = "APPOINTMENTDATETIME", nullable = false)
    private LocalDateTime appointmentDateTime;

    @NotNull
    @Column(name = "ENDDATETIME", nullable = false)
    private LocalDateTime endDateTime;

    @Size(max = 20)
    @ColumnDefault("'Confirmed'")
    @Column(name = "STATUS", length = 20)
    private String status;

    @ManyToMany
    @JoinTable(name = "APPOINTMENT_DETAIL",
            joinColumns = @JoinColumn(name = "APPOINTMENTID", referencedColumnName = "APPOINTMENTID"),
            inverseJoinColumns = @JoinColumn(name = "SERVICEID", referencedColumnName = "SERVICEID"))
    private Set<SalonService> services = new LinkedHashSet<>();


}