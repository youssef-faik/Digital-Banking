package com.example.digitalbanking.config;

import com.example.digitalbanking.entities.*;
import com.example.digitalbanking.repositories.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
@Slf4j
@Transactional
public class DataSeeder implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final CustomerRepository customerRepository;
    private final BankAccountRepository bankAccountRepository;
    private final AccountOperationRepository accountOperationRepository;
    private final PasswordEncoder passwordEncoder;
    private final Random random = new Random();

    public DataSeeder(AppUserRepository appUserRepository, 
                     RoleRepository roleRepository,
                     CustomerRepository customerRepository,
                     BankAccountRepository bankAccountRepository,
                     AccountOperationRepository accountOperationRepository,
                     PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.customerRepository = customerRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.accountOperationRepository = accountOperationRepository;
        this.passwordEncoder = passwordEncoder;
    }    @Override
    public void run(String... args) throws Exception {
        if (shouldSeedData()) {
            log.info("Starting data seeding...");
            seedData();
            log.info("Data seeding completed successfully!");
        } else {
            log.info("Data already exists, but checking if sample data is needed...");
            // Check if existing users need sample data
            seedSampleDataForExistingUsers();
        }
    }

    private boolean shouldSeedData() {
        // Check if data already exists
        return appUserRepository.count() == 0;
    }
    
    private void seedSampleDataForExistingUsers() {
        // Check if any user has customers and accounts
        long totalCustomers = customerRepository.count();
        long totalAccounts = bankAccountRepository.count();
        
        if (totalCustomers == 0 || totalAccounts == 0) {
            log.info("Found existing users but no sample data. Creating sample data...");
            
            // Get existing users (excluding admin if present)
            List<AppUser> existingUsers = appUserRepository.findAll().stream()
                .filter(user -> !user.getUsername().equals("admin"))
                .limit(3) // Limit to first 3 non-admin users
                .toList();
                
            if (!existingUsers.isEmpty()) {
                for (AppUser user : existingUsers) {
                    createCustomersAndAccountsForUser(user, 3, 5);
                }
                
                // Create sample operations for all accounts
                createSampleOperations();
                log.info("Sample data created for existing users!");
            }
        } else {
            log.info("Sample data already exists, no action needed.");
        }
    }

    private void seedData() {
        // Create roles
        Role adminRole = createRoleIfNotExists("ADMIN");
        Role userRole = createRoleIfNotExists("USER");

        // Create admin user
        AppUser adminUser = createAdminUser(adminRole, userRole);
        
        // Create demo users
        AppUser demoUser1 = createDemoUser("john_doe", "john.doe@example.com", userRole);
        AppUser demoUser2 = createDemoUser("jane_smith", "jane.smith@example.com", userRole);
        AppUser demoUser3 = createDemoUser("bob_wilson", "bob.wilson@example.com", userRole);

        // Create customers and accounts for each user
        createCustomersAndAccountsForUser(demoUser1, 3, 5);
        createCustomersAndAccountsForUser(demoUser2, 2, 4);
        createCustomersAndAccountsForUser(demoUser3, 4, 6);

        // Create sample operations for all accounts
        createSampleOperations();
    }

    private Role createRoleIfNotExists(String roleName) {
        return roleRepository.findByRoleName(roleName)
            .orElseGet(() -> {
                Role role = new Role();
                role.setRoleName(roleName);
                return roleRepository.save(role);
            });
    }

    private AppUser createAdminUser(Role adminRole, Role userRole) {
        AppUser adminUser = new AppUser();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@digitalbanking.com");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setRoles(List.of(adminRole, userRole));
        return appUserRepository.save(adminUser);
    }

    private AppUser createDemoUser(String username, String email, Role userRole) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRoles(List.of(userRole));
        return appUserRepository.save(user);
    }

    private void createCustomersAndAccountsForUser(AppUser user, int customerCount, int accountCount) {
        log.info("Creating {} customers and {} accounts for user: {}", customerCount, accountCount, user.getUsername());
        
        List<Customer> customers = new ArrayList<>();
        
        // Create customers
        for (int i = 1; i <= customerCount; i++) {
            Customer customer = new Customer();
            customer.setName(generateCustomerName(user.getUsername(), i));
            customer.setEmail(generateCustomerEmail(user.getUsername(), i));
            customer.setAppUser(user);
            customers.add(customerRepository.save(customer));
        }

        // Create accounts for customers
        int accountsCreated = 0;
        for (Customer customer : customers) {
            int accountsPerCustomer = Math.min(3, accountCount - accountsCreated);
            
            for (int i = 0; i < accountsPerCustomer; i++) {
                if (accountsCreated >= accountCount) break;
                
                // Randomly choose between Current and Saving account
                if (random.nextBoolean()) {
                    createCurrentAccount(customer);
                } else {
                    createSavingAccount(customer);
                }
                accountsCreated++;
            }
            
            if (accountsCreated >= accountCount) break;
        }
    }

    private String generateCustomerName(String username, int index) {
        String[] firstNames = {"Alex", "Maria", "David", "Sarah", "Michael", "Emma", "James", "Lisa", "Robert", "Amy"};
        String[] lastNames = {"Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez", "Hernandez"};
        
        return firstNames[random.nextInt(firstNames.length)] + " " + 
               lastNames[random.nextInt(lastNames.length)] + " " + index;
    }

    private String generateCustomerEmail(String username, int index) {
        return username + "_customer" + index + "@example.com";
    }

    private void createCurrentAccount(Customer customer) {
        CurrentAccount account = new CurrentAccount();
        account.setId(UUID.randomUUID().toString());
        account.setBalance(BigDecimal.valueOf(1000 + random.nextDouble() * 9000)); // 1000-10000
        account.setOverdraft(BigDecimal.valueOf(500 + random.nextDouble() * 1500)); // 500-2000
        account.setCreatedAt(Instant.now().minus(random.nextInt(90), ChronoUnit.DAYS));
        account.setCustomer(customer);
        bankAccountRepository.save(account);
    }

    private void createSavingAccount(Customer customer) {
        SavingAccount account = new SavingAccount();
        account.setId(UUID.randomUUID().toString());
        account.setBalance(BigDecimal.valueOf(2000 + random.nextDouble() * 18000)); // 2000-20000
        account.setInterestRate(BigDecimal.valueOf(0.01 + random.nextDouble() * 0.04)); // 1%-5%
        account.setCreatedAt(Instant.now().minus(random.nextInt(90), ChronoUnit.DAYS));
        account.setCustomer(customer);
        bankAccountRepository.save(account);
    }

    private void createSampleOperations() {
        log.info("Creating sample operations for all accounts...");
        
        List<BankAccount> allAccounts = bankAccountRepository.findAll();
        
        for (BankAccount account : allAccounts) {
            createOperationsForAccount(account);
        }
    }

    private void createOperationsForAccount(BankAccount account) {
        AppUser user = account.getCustomer().getAppUser();
        int operationCount = 5 + random.nextInt(15); // 5-20 operations per account
        
        for (int i = 0; i < operationCount; i++) {
            AccountOperation operation = new AccountOperation();
            operation.setBankAccount(account);
            operation.setAppUser(user);
            operation.setOperationDate(generateRandomDate());
            
            // Randomly choose operation type
            OperationType operationType = random.nextBoolean() ? OperationType.CREDIT : OperationType.DEBIT;
            operation.setType(operationType);
            
            // Generate amount
            BigDecimal amount = BigDecimal.valueOf(10 + random.nextDouble() * 990); // 10-1000
            operation.setAmount(amount);
            
            // Generate description
            operation.setDescription(generateOperationDescription(operationType));
            
            accountOperationRepository.save(operation);
        }
    }

    private Instant generateRandomDate() {
        // Generate dates within the last 30 days
        int daysAgo = random.nextInt(30);
        int hoursAgo = random.nextInt(24);
        int minutesAgo = random.nextInt(60);
        
        return Instant.now()
            .minus(daysAgo, ChronoUnit.DAYS)
            .minus(hoursAgo, ChronoUnit.HOURS)
            .minus(minutesAgo, ChronoUnit.MINUTES);
    }

    private String generateOperationDescription(OperationType type) {
        if (type == OperationType.CREDIT) {
            String[] creditDescriptions = {
                "Salary deposit", "Transfer received", "Investment return", 
                "Bonus payment", "Refund", "Interest payment", "Gift received"
            };
            return creditDescriptions[random.nextInt(creditDescriptions.length)];
        } else {
            String[] debitDescriptions = {
                "ATM withdrawal", "Online purchase", "Bill payment", 
                "Transfer sent", "Service fee", "Grocery shopping", "Gas station"
            };
            return debitDescriptions[random.nextInt(debitDescriptions.length)];
        }
    }
}
