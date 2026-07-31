package com.fakemed.config;

import com.fakemed.model.Role;
import com.fakemed.model.User;
import com.fakemed.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            System.out.println("=========================================================");
            System.out.println(" Default admin account created -> username: admin / password: admin123");
            System.out.println(" CHANGE THIS PASSWORD before using this in production.");
            System.out.println("=========================================================");
        }
    }
}
