package Fitspan.demo_fitSpan.configuration;


import Fitspan.demo_fitSpan.constant.PredefinedRole;
import Fitspan.demo_fitSpan.entity.Role;
import Fitspan.demo_fitSpan.entity.User;
import Fitspan.demo_fitSpan.repository.RoleRepository;
import Fitspan.demo_fitSpan.repository.UserRepository;
import lombok.AccessLevel;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.HashSet;


@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @NonFinal
    static final String ADMIN_USER_NAME = "admin";

    @NonFinal
    static final String ADMIN_PASSWORD = "admin";

    @Bean
    @ConditionalOnProperty(
            prefix = "spring",
            value = "datasource.driverClassName",
            havingValue = "org.postgresql.Driver"
    )
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        log.info("admin haven't create yet");
        return args -> {
            if (userRepository.findByUsername(ADMIN_USER_NAME).isEmpty()) {
                if (roleRepository.findById(PredefinedRole.USER_ROLE).isEmpty()) {
                    roleRepository.save(Role.builder()
                            .name(PredefinedRole.USER_ROLE)
                            .description("User role")
                            .build());
                    log.info("User role ddc tao");
                }

                if (roleRepository.findById(PredefinedRole.ADMIN_ROLE).isEmpty()) {
                    Role adminRole = roleRepository.save(Role.builder()
                            .name(PredefinedRole.ADMIN_ROLE)
                            .description("Admin role")
                            .build());
                    var roles = new HashSet<Role>();
                    roles.add(adminRole);

                    User user = User.builder()
                            .username(ADMIN_USER_NAME)
                            .password(passwordEncoder.encode(ADMIN_PASSWORD))
                            .roles(roles)
                            .build();
                    log.info("Admin role dc tao");

                    userRepository.save(user);
                }

                log.warn("admin user has been created with default password: admin, please change it");
            }
            log.info("Application initialization completed .....");
        };
    }
}