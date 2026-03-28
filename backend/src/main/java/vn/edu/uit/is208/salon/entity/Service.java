package vn.edu.uit.is208.salon.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "SERVICE")
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SERVICEID", nullable = false)
    private Long id;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @NotNull
    @Column(name = "PRICE", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull
    @Column(name = "DURATIONMINUTES", nullable = false)
    private Long durationMinutes;

    @ManyToMany(mappedBy = "services")
    private Set<Appointment> appointments = new LinkedHashSet<>();


}