package com.sena.backend.config;

import com.sena.backend.entity.Role;
import com.sena.backend.entity.User;
import com.sena.backend.repository.RoleRepository;
import com.sena.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Component responsible for bootstrapping the initial administrative account at startup.
 * <p>
 * This service ensures the existence of the primary 'admin' user upon application
 * initialization. It performs secure password hashing at runtime using the
 * configured {@link Argon2PasswordEncoder} and assigns the required administrative
 * privileges, leveraging external environment variables to maintain credential security.
 * <p>
 * Excluded under the "test" profile (@Profile("!test")): in that profile
 * application-test.yml disables Flyway and uses ddl-auto: create, so the
 * roles table starts empty — this runner's job is bootstrapping a real
 * deployment, not every integration-test run, and the seed data a single
 * test needs (if any) belongs in that test's own @BeforeEach, not here.
 */

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_ROLE = "ROLE_ADMIN";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final Argon2PasswordEncoder passwordEncoder;

    @Value("${admin.setup.password:admin123}")
    private String adminSetupPassword;

    public DataInitializer(UserRepository userRepository,
                           RoleRepository roleRepository,
                           Argon2PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername(ADMIN_USERNAME).isPresent()) {
            log.info("Admin user already exists; skipping bootstrap.");
            return;
        }

        Role adminRole = roleRepository.findByName(ADMIN_ROLE)
                .orElseThrow(() -> new IllegalStateException(
                        "Role " + ADMIN_ROLE + " not found. Ensure the Flyway role seed has run."));

        User admin = new User();
        admin.setUsername(ADMIN_USERNAME);
        admin.setPassword(passwordEncoder.encode(adminSetupPassword));
        admin.setRole(adminRole);
        admin.setActive(true);

        userRepository.save(admin);
        log.info("Bootstrapped initial admin user '{}' with role {}.", ADMIN_USERNAME, ADMIN_ROLE);
    }
}