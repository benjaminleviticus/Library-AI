package com.bookmanagement.bookmanagementsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.bookmanagement.bookmanagementsystem.repository.AdminRepository;
import com.bookmanagement.bookmanagementsystem.entity.Admin;

@SpringBootApplication
public class BookManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookManagementSystemApplication.class, args);
    }

    // Initialize the first admin if none exists.
    // Set ADMIN_USERNAME / ADMIN_PASSWORD in the environment to avoid the built-in defaults.
    @Bean
    CommandLineRunner initAdmin(AdminRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (repo.count() == 0) {
                String username = System.getenv("ADMIN_USERNAME");
                String password = System.getenv("ADMIN_PASSWORD");
                if (username == null || username.isBlank() || password == null || password.isBlank()) {
                    throw new IllegalStateException(
                        "No admin exists. Set ADMIN_USERNAME and ADMIN_PASSWORD environment variables for first startup.");
                }
                repo.save(Admin.builder()
                        .username(username)
                        .password(encoder.encode(password))
                        .build());
            }
        };
    }
}
