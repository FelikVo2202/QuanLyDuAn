package vn.edu.uit.is208.salon.seeder;

import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import vn.edu.uit.is208.salon.constant.*;
import vn.edu.uit.is208.salon.entity.*;
import vn.edu.uit.is208.salon.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DummyDataSeeder implements CommandLineRunner {

    private static final List<String> SERVICES = List.of(
            "Men's haircut", "Women's haircut", "Hair perm", "Hair coloring",
            "Shampoo & massage", "Hair straightening", "Hair repair treatment",
            "Bridal styling", "Beard trim", "Keratin hair straightening"
    );
    private static final List<String> PRODUCT_CATEGORIES = List.of(
            "Shampoo", "Conditioner", "Hair dye", "Hair care",
            "Wax/Gel", "Essential oil", "Tools"
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
    private final ProductRepository productRepository;
    private final ServiceRecipeRepository serviceRecipeRepository;
    private final InventoryLedgerRepository inventoryLedgerRepository;
    private final BillRepository billRepository;

    private final PasswordEncoder passwordEncoder;
    private final Faker faker = new Faker(new Locale("en"));

    @Override
    @Transactional
    public void run(String... args) {
        if (customerRepository.count() > 0) {
            System.out.println("[Seeder] Data already exists, skipping seeding.");
            return;
        }

        System.out.println("[Seeder] Starting dummy data seeding...");

        List<Customer> customers = seedCustomers(20);
        List<Staff> staffList = seedStaff(10);
        List<SalonService> services = seedServices();
        List<Appointment> appointments = seedAppointments(30, customers, staffList, services);
        List<Product> products = seedProducts(30);
        seedServiceRecipes(services, products);
        seedInventoryLedger(products);
        List<Bill> bills = seedBills(appointments, products);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                System.out.println("[Seeder] Done! Created: \n"
                        + "- " + customers.size() + " customers\n"
                        + "- " + staffList.size() + " staff members\n"
                        + "- " + services.size() + " services\n"
                        + "- " + appointments.size() + " appointments\n"
                        + "- " + products.size() + " products (with recipes & inventory)\n"
                        + "- " + bills.size() + " bills.");
            }
        });
    }

    private List<Customer> seedCustomers(int count) {
        List<Customer> customers = new ArrayList<>();
        String[] genders = {"Male", "Female", "Other"};

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

        // 1. Create default test accounts
        Staff manager = new Staff();
        manager.setFirstName("Admin");
        manager.setLastName("Manager");
        manager.setRole(StaffRole.MANAGER);
        manager.setUsername("manager");
        manager.setPasswordHash(encodedPassword);
        staffList.add(manager);

        Staff receptionist = new Staff();
        receptionist.setFirstName("Reception");
        receptionist.setLastName("Staff");
        receptionist.setRole(StaffRole.RECEPTIONIST);
        receptionist.setUsername("receptionist");
        receptionist.setPasswordHash(encodedPassword);
        staffList.add(receptionist);

        Staff stylist = new Staff();
        stylist.setFirstName("Stylist");
        stylist.setLastName("Staff");
        stylist.setRole(StaffRole.STYLIST);
        stylist.setUsername("stylist");
        stylist.setPasswordHash(encodedPassword);
        staffList.add(stylist);

        // 2. Create additional random staff to reach the requested count (excluding the 3 accounts above)
        int randomCount = Math.max(0, count - 3);
        for (int i = 0; i < randomCount; i++) {
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

    private List<Appointment> seedAppointments(int count,
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

        return appointmentRepository.saveAll(appointments);
    }

    private List<Product> seedProducts(int count) {
        List<Product> products = new ArrayList<>();
        ProductType[] types = ProductType.values();

        for (int i = 0; i < count; i++) {
            Product product = new Product();
            product.setName(faker.commerce().productName() + " " + faker.commerce().material());
            product.setCategory(PRODUCT_CATEGORIES.get(faker.random().nextInt(PRODUCT_CATEGORIES.size())));
            product.setProductType(types[faker.random().nextInt(types.length)]);
            product.setPrice(BigDecimal.valueOf(faker.number().numberBetween(50, 1500) * 1000L));

            product.setBaseUom("ml");
            product.setPurchasingUom("Bottle");
            product.setConversionFactor(BigDecimal.valueOf(faker.number().numberBetween(100, 1000)));

            product.setQuantityOnHand(BigDecimal.valueOf(faker.number().randomDouble(2, 10000, 20000)));

            products.add(product);
        }

        return productRepository.saveAll(products);
    }

    private void seedServiceRecipes(List<SalonService> services, List<Product> products) {
        List<ServiceRecipe> recipes = new ArrayList<>();
        Random rnd = new Random();

        for (SalonService service : services) {
            int productCount = rnd.nextInt(4);
            Set<Product> chosenProducts = new HashSet<>();

            while (chosenProducts.size() < productCount) {
                chosenProducts.add(products.get(rnd.nextInt(products.size())));
            }

            for (Product product : chosenProducts) {
                ServiceRecipe recipe = new ServiceRecipe();

                ServiceRecipeId id = new ServiceRecipeId();
                id.setServiceId(service.getId());
                id.setProductId(product.getId());
                recipe.setId(id);

                recipe.setService(service);
                recipe.setProduct(product);

                recipe.setQuantityConsumed(BigDecimal.valueOf(faker.number().randomDouble(2, 5, 50)));

                recipes.add(recipe);
            }
        }
        serviceRecipeRepository.saveAll(recipes);
    }

    private void seedInventoryLedger(List<Product> products) {
        List<InventoryLedger> ledgers = new ArrayList<>();
        InventoryTransactionType[] types = InventoryTransactionType.values();
        InventoryTransactionType defaultStockInType = types.length > 0 ? types[0] : null;

        for (Product product : products) {
            InventoryLedger ledger = new InventoryLedger();
            ledger.setProduct(product);
            ledger.setChangeAmount(product.getQuantityOnHand());
            ledger.setTransactionType(defaultStockInType);
            ledger.setTransactionDate(LocalDateTime.now().minusDays(faker.number().numberBetween(5, 60)));
            ledgers.add(ledger);
        }
        inventoryLedgerRepository.saveAll(ledgers);
    }

    private List<Bill> seedBills(List<Appointment> appointments, List<Product> products) {
        List<Bill> bills = new ArrayList<>();
        Random rnd = new Random();
        PaymentStatus[] paymentStatuses = PaymentStatus.values();

        for (Appointment appointment : appointments) {
            if (appointment.getStatus() == AppointmentStatus.CANCELED) {
                continue;
            }

            Bill bill = new Bill();
            bill.setAppointment(appointment);
            bill.setCustomer(appointment.getCustomer());
            bill.setBillDate(appointment.getEndDateTime() != null ? appointment.getEndDateTime() : LocalDateTime.now());

            if (appointment.getStatus() == AppointmentStatus.PAID || appointment.getStatus() == AppointmentStatus.DONE) {
                bill.setPaymentStatus(paymentStatuses.length > 1 ? paymentStatuses[paymentStatuses.length - 1] : paymentStatuses[0]);
            } else {
                bill.setPaymentStatus(paymentStatuses[rnd.nextInt(paymentStatuses.length)]);
            }

            BigDecimal totalAmount = BigDecimal.ZERO;
            Set<BillDetail> details = new LinkedHashSet<>();

            if (appointment.getServices() != null) {
                for (SalonService service : appointment.getServices()) {
                    BillDetail detail = new BillDetail();
                    detail.setBill(bill);
                    detail.setService(service);
                    detail.setQuantity(1L);
                    detail.setUnitPrice(service.getPrice());

                    totalAmount = totalAmount.add(service.getPrice());
                    details.add(detail);
                }
            }

            int extraProductsCount = rnd.nextInt(3);
            for (int i = 0; i < extraProductsCount; i++) {
                Product randomProduct = products.get(rnd.nextInt(products.size()));

                BillDetail detail = new BillDetail();
                detail.setBill(bill);
                detail.setProduct(randomProduct);

                long qty = faker.number().numberBetween(1, 3);
                detail.setQuantity(qty);
                detail.setUnitPrice(randomProduct.getPrice());

                BigDecimal itemTotal = randomProduct.getPrice().multiply(BigDecimal.valueOf(qty));
                totalAmount = totalAmount.add(itemTotal);

                details.add(detail);
            }

            bill.setDetails(details);
            bill.setTotalAmount(totalAmount);
            bills.add(bill);
        }

        return billRepository.saveAll(bills);
    }

    private String generateVietnamesePhone() {
        String[] prefixes = {"032", "033", "034", "035", "036",
                "038", "039", "090", "091", "094",
                "070", "079", "077", "076", "078"};
        String prefix = prefixes[faker.random().nextInt(prefixes.length)];
        return prefix + faker.number().digits(7);
    }
}