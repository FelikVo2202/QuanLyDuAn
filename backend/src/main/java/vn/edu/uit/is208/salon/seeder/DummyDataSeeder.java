package vn.edu.uit.is208.salon.seeder;

import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.uit.is208.salon.constant.AppointmentStatus;
import vn.edu.uit.is208.salon.constant.StaffRole;
import vn.edu.uit.is208.salon.entity.Appointment;
import vn.edu.uit.is208.salon.entity.Customer;
import vn.edu.uit.is208.salon.entity.SalonService;
import vn.edu.uit.is208.salon.entity.Staff;
import vn.edu.uit.is208.salon.repository.AppointmentRepository;
import vn.edu.uit.is208.salon.repository.CustomerRepository;
import vn.edu.uit.is208.salon.repository.SalonServiceRepository;
import vn.edu.uit.is208.salon.repository.StaffRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DummyDataSeeder implements CommandLineRunner {

    private static final List<String> SERVICES = List.of(
            "Cắt tóc nam", "Cắt tóc nữ", "Uốn tóc", "Nhuộm tóc",
            "Gội đầu massage", "Ép tóc", "Phục hồi tóc hư tổn",
            "Tạo kiểu cô dâu", "Cắt tỉa râu", "Duỗi tóc Keratin"
    );
    private static final List<StaffRole> STAFF_ROLES = List.of(
            StaffRole.MANAGER,
            StaffRole.RECEPTIONIST,
            StaffRole.STYLIST
    );
    private static final List<AppointmentStatus> APPOINTMENT_STATUSES = List.of(
            AppointmentStatus.CONFIRMED,
            AppointmentStatus.PAID,
            AppointmentStatus.DONE,
            AppointmentStatus.CANCELED
    );
    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;
    private final SalonServiceRepository salonServiceRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final Faker faker = new Faker(new Locale("vi"));

    @Override
    @Transactional
    public void run(String... args) {
        if (customerRepository.count() > 0) {
            System.out.println("[Seeder] Dữ liệu đã tồn tại, bỏ qua seeding.");
            return;
        }

        System.out.println("[Seeder] Bắt đầu tạo dữ liệu giả...");

        List<Customer> customers = seedCustomers(20);
        List<Staff> staffList = seedStaff(10);
        List<SalonService> services = seedServices();
        seedAppointments(30, customers, staffList, services);

        System.out.println("[Seeder] Hoàn tất! Đã tạo: "
                + customers.size() + " khách hàng, "
                + staffList.size() + " nhân viên, "
                + services.size() + " dịch vụ, "
                + "30 lịch hẹn.");
    }

    private List<Customer> seedCustomers(int count) {
        List<Customer> customers = new ArrayList<>();
        String[] genders = {"Nam", "Nữ", "Khác"};

        for (int i = 0; i < count; i++) {
            Customer customer = new Customer();
            customer.setFirstName(faker.name().firstName());
            customer.setLastName(faker.name().lastName());
            customer.setPhoneNumber(generateVietnamesePhone());
            customer.setEmail(faker.internet().emailAddress());
            customer.setGender(genders[faker.random().nextInt(genders.length)]);
            customers.add(customer);
        }

        return customerRepository.saveAll(customers);
    }

    private List<Staff> seedStaff(int count) {
        List<Staff> staffList = new ArrayList<>();

        String encodedPassword = passwordEncoder.encode("123456");

        for (int i = 0; i < count; i++) {
            Staff staff = new Staff();
            staff.setFirstName(faker.name().firstName());
            staff.setLastName(faker.name().lastName());
            staff.setRole(STAFF_ROLES.get(faker.random().nextInt(STAFF_ROLES.size())));
            staff.setUsername(faker.name().firstName().toLowerCase().replaceAll("\\s+", "") + faker.number().digits(4));
            staff.setPasswordHash(encodedPassword);
            staffList.add(staff);
        }

        return staffRepository.saveAll(staffList);
    }

    private List<SalonService> seedServices() {
        List<SalonService> services = new ArrayList<>();

        for (String serviceName : SERVICES) {
            SalonService service = new SalonService();
            service.setName(serviceName);
            service.setPrice(BigDecimal.valueOf(
                    faker.number().numberBetween(50_000L, 500_000L)));
            service.setDurationMinutes((long) faker.number().numberBetween(15, 120));
            services.add(service);
        }

        return salonServiceRepository.saveAll(services);
    }

    private void seedAppointments(int count,
                                  List<Customer> customers,
                                  List<Staff> staffList,
                                  List<SalonService> services) {
        List<Appointment> appointments = new ArrayList<>();
        Random rnd = new Random();

        for (int i = 0; i < count; i++) {
            Appointment appointment = new Appointment();

            appointment.setCustomer(customers.get(rnd.nextInt(customers.size())));
            appointment.setStaff(staffList.get(rnd.nextInt(staffList.size())));

            long offsetDays = faker.number().numberBetween(-30, 30);
            long offsetHours = faker.number().numberBetween(8, 20);
            LocalDateTime dateTime = LocalDateTime.now()
                    .plus(offsetDays, ChronoUnit.DAYS)
                    .truncatedTo(ChronoUnit.DAYS)
                    .plus(offsetHours, ChronoUnit.HOURS);
            appointment.setAppointmentDateTime(dateTime);
            appointment.setEndDateTime(dateTime.plusHours(1));

            appointment.setStatus(
                    APPOINTMENT_STATUSES.get(rnd.nextInt(APPOINTMENT_STATUSES.size())));

            Set<SalonService> chosenServices = new LinkedHashSet<>();
            int serviceCount = faker.number().numberBetween(1, 4);
            List<SalonService> shuffled = new ArrayList<>(services);
            Collections.shuffle(shuffled, rnd);
            chosenServices.addAll(shuffled.subList(0, serviceCount));
            appointment.setServices(chosenServices);

            appointments.add(appointment);
        }

        appointmentRepository.saveAll(appointments);
    }

    private String generateVietnamesePhone() {
        String[] prefixes = {"032", "033", "034", "035", "036",
                "038", "039", "090", "091", "094",
                "070", "079", "077", "076", "078"};
        String prefix = prefixes[faker.random().nextInt(prefixes.length)];
        return prefix + faker.number().digits(7);
    }
}