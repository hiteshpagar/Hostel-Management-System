package com.hms.config;

import com.hms.common.enums.RoleType;
import com.hms.common.enums.UserStatus;
import com.hms.entity.User;
import com.hms.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.default-admin.email:superadmin@hms.com}")
    private String adminEmail;

    @Value("${app.default-admin.password:SuperAdmin@123}")
    private String adminPassword;

    @Value("${app.default-admin.first-name:Super}")
    private String firstName;

    @Value("${app.default-admin.last-name:Admin}")
    private String lastName;

    @Value("${app.default-admin.phone:+919876543210}")
    private String phone;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedSuperAdminIfNotExists();
    }

    private void seedSuperAdminIfNotExists() {
        if (!userRepository.existsByEmail(adminEmail)) {
            log.info("No super admin found. Seeding initial super admin account: {}", adminEmail);

            User superAdmin = new User();
            superAdmin.setFirstName(firstName);
            superAdmin.setLastName(lastName);
            superAdmin.setEmail(adminEmail.toLowerCase().trim());
            superAdmin.setPhoneNumber(phone);
            superAdmin.setPasswordHash(passwordEncoder.encode(adminPassword));
            superAdmin.setRole(RoleType.ROLE_SUPER_ADMIN);
            superAdmin.setMustChangePassword(false);
            superAdmin.setStatus(UserStatus.ACTIVE);
            superAdmin.setHostel(null); // Platform super admin is not bound to a single hostel

            userRepository.save(superAdmin);
            log.info("Default Super Admin successfully initialized.");
        } else {
            log.info("Super admin account already exists: {}", adminEmail);
        }
    }
}
