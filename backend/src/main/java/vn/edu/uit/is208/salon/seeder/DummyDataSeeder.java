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

    private record TimeSlot(LocalDateTime start, LocalDateTime end) {}

    @Override
    @Transactional
    public void run(String... args) {
        if (customerRepository.count() > 0) {
            System.out.println("[Seeder] Data already exists, skipping seeding.");
            return;
        }

        System.out.println("[Seeder] Starting dummy data seeding...");

        List<Customer> customers = seedCustomers(50);
        List<Staff> staffList = seedStaff(12);
        List<SalonService> services = seedServices();
        List<Appointment> appointments = seedAppointments(200, customers, staffList, services);
        List<Product> products = seedProducts(30);
        seedServiceRecipes(services, products);
        seedInventoryLedger(products);

        List<Product> sellableProducts = products.stream()
                .filter(p -> p.getProductType() == ProductType.RETAIL || p.getProductType() == ProductType.BOTH)
                .toList();

        List<Bill> bills = seedBills(appointments, sellableProducts);
        List<Bill> retailBills = seedRetailBills(customers, sellableProducts);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                System.out.println("[Seeder] Done! Created: \n"
                        + "- " + customers.size() + " customers\n"
                        + "- " + staffList.size() + " staff members\n"
                        + "- " + services.size() + " services\n"
                        + "- " + appointments.size() + " appointments (no overlapping for active slots)\n"
                        + "- " + products.size() + " products (with recipes & inventory)\n"
                        + "- " + (bills.size() + retailBills.size()) + " bills (" + retailBills.size() + " retail bills).");
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
            service.setPrice(BigDecimal.valueOf(faker.number().numberBetween(50, 500) * 1000L));
            long durationBlocks = faker.number().numberBetween(3, 5);
            service.setDurationMinutes(durationBlocks * 15);
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
        LocalDateTime now = LocalDateTime.now();

        int[] minuteBlocks = {0, 15, 30, 45};

        List<Staff> stylists = staffList.stream()
                .filter(staff -> staff.getRole() == StaffRole.STYLIST)
                .toList();

        if (stylists.isEmpty()) {
            throw new IllegalStateException("Cannot seed appointments: No staff members with STYLIST role found.");
        }

        Map<Long, List<TimeSlot>> staffBusySlots = new HashMap<>();
        for (Staff s : stylists) {
            staffBusySlots.put(s.getId(), new ArrayList<>());
        }

        for (int i = 0; i < count; i++) {
            Appointment appointment = new Appointment();

            appointment.setCustomer(customers.get(rnd.nextInt(customers.size())));

            Staff selectedStaff = stylists.get(rnd.nextInt(stylists.size()));
            appointment.setStaff(selectedStaff);

            Set<SalonService> chosenServices = new LinkedHashSet<>();
            int serviceCount = faker.number().numberBetween(1, 4);
            List<SalonService> shuffled = new ArrayList<>(services);
            Collections.shuffle(shuffled, rnd);
            chosenServices.addAll(shuffled.subList(0, serviceCount));
            appointment.setServices(chosenServices);

            long totalDurationMinutes = chosenServices.stream()
                    .mapToLong(SalonService::getDurationMinutes)
                    .sum();

            long offsetDays;
            long offsetHours = faker.number().numberBetween(8, 19);

            if (i < count * 0.75) {
                offsetDays = faker.number().numberBetween(0, 8);
            } else {
                offsetDays = faker.number().numberBetween(-15, 0);
            }

            int randomMinute = minuteBlocks[rnd.nextInt(minuteBlocks.length)];

            LocalDateTime startDateTime = now.plusDays(offsetDays)
                    .truncatedTo(ChronoUnit.DAYS)
                    .plusHours(offsetHours)
                    .plusMinutes(randomMinute);

            LocalDateTime endDateTime = startDateTime.plusMinutes(totalDurationMinutes);

            boolean isCanceled = rnd.nextDouble() >= 0.85;

            if (!isCanceled) {
                List<TimeSlot> busySlots = staffBusySlots.get(selectedStaff.getId());

                while (isOverlapping(startDateTime, endDateTime, busySlots)) {
                    startDateTime = startDateTime.plusMinutes(15);
                    endDateTime = startDateTime.plusMinutes(totalDurationMinutes);

                    if (startDateTime.getHour() >= 20) {
                        startDateTime = startDateTime.plusDays(1)
                                .truncatedTo(ChronoUnit.DAYS)
                                .plusHours(8);
                        endDateTime = startDateTime.plusMinutes(totalDurationMinutes);
                    }
                }

                busySlots.add(new TimeSlot(startDateTime, endDateTime));

                if (endDateTime.isBefore(now)) {
                    appointment.setStatus(AppointmentStatus.DONE);
                } else {
                    appointment.setStatus(AppointmentStatus.CONFIRMED);
                }
            } else {
                appointment.setStatus(AppointmentStatus.CANCELED);
            }

            appointment.setAppointmentDateTime(startDateTime);
            appointment.setEndDateTime(endDateTime);

            appointments.add(appointment);
        }

        appointments.sort(Comparator.comparing(Appointment::getAppointmentDateTime));
        return appointmentRepository.saveAll(appointments);
    }

    private boolean isOverlapping(LocalDateTime start, LocalDateTime end, List<TimeSlot> existingSlots) {
        for (TimeSlot slot : existingSlots) {
            if (start.isBefore(slot.end()) && end.isAfter(slot.start())) {
                return true;
            }
        }
        return false;
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

        for (Appointment appointment : appointments) {
            if (appointment.getStatus() == AppointmentStatus.CANCELED ||
                    appointment.getStatus() == AppointmentStatus.CONFIRMED) {
                continue;
            }

            Bill bill = new Bill();
            bill.setAppointment(appointment);
            bill.setCustomer(appointment.getCustomer());
            bill.setBillDate(appointment.getEndDateTime() != null ? appointment.getEndDateTime() : LocalDateTime.now());

            bill.setPaymentStatus(PaymentStatus.PAID);

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

        bills.sort(Comparator.comparing(Bill::getBillDate));
        return billRepository.saveAll(bills);
    }

    private List<Bill> seedRetailBills(List<Customer> customers, List<Product> products) {
        List<Bill> retailBills = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        Random rnd = new Random();

        for (int monthOffset = 6; monthOffset >= 0; monthOffset--) {
            long targetRevenue = (monthOffset == 0) ? 150_000_000L : faker.number().numberBetween(200_000_000L, 300_000_000L);
            long currentRevenue = 0;

            LocalDateTime startOfMonth;
            LocalDateTime endOfMonth;

            if (monthOffset == 0) {
                startOfMonth = now.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
                endOfMonth = now; // up to current time
            } else {
                startOfMonth = now.minusMonths(monthOffset).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
                endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
            }

            long daysBetween = ChronoUnit.DAYS.between(startOfMonth, endOfMonth);
            if (daysBetween <= 0) daysBetween = 1;

            while (currentRevenue < targetRevenue) {
                Bill bill = new Bill();
                bill.setCustomer(customers.get(rnd.nextInt(customers.size())));

                long randomDays = faker.number().numberBetween(0L, daysBetween + 1);
                LocalDateTime randomDate = startOfMonth.plusDays(randomDays).plusHours(faker.number().numberBetween(8, 20));
                if (randomDate.isAfter(endOfMonth)) {
                    randomDate = endOfMonth;
                }
                bill.setBillDate(randomDate);
                bill.setPaymentStatus(PaymentStatus.PAID);

                BigDecimal totalAmount = BigDecimal.ZERO;
                Set<BillDetail> details = new LinkedHashSet<>();

                int productCount = faker.number().numberBetween(1, 4);
                for (int i = 0; i < productCount; i++) {
                    Product randomProduct = products.get(rnd.nextInt(products.size()));

                    BillDetail detail = new BillDetail();
                    detail.setBill(bill);
                    detail.setProduct(randomProduct);

                    long qty = faker.number().numberBetween(1, 4);
                    detail.setQuantity(qty);
                    detail.setUnitPrice(randomProduct.getPrice());

                    BigDecimal itemTotal = randomProduct.getPrice().multiply(BigDecimal.valueOf(qty));
                    totalAmount = totalAmount.add(itemTotal);

                    details.add(detail);
                }

                bill.setDetails(details);
                bill.setTotalAmount(totalAmount);
                retailBills.add(bill);

                currentRevenue += totalAmount.longValue();
            }
        }

        retailBills.sort(Comparator.comparing(Bill::getBillDate));
        return billRepository.saveAll(retailBills);
    }

    private String generateVietnamesePhone() {
        String[] prefixes = {"032", "033", "034", "035", "036",
                "038", "039", "090", "091", "094",
                "070", "079", "077", "076", "078"};
        String prefix = prefixes[faker.random().nextInt(prefixes.length)];
        return prefix + faker.number().digits(7);
    }
}