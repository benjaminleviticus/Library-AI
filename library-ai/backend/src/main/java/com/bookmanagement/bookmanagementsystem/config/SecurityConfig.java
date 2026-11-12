package com.bookmanagement.bookmanagementsystem.config;

import com.bookmanagement.bookmanagementsystem.repository.AdminRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // optional if using simple forms
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/index.html", "/reader.html",
                    "/login.html",
                    "/css/**", "/js/**", "/images/**",
                    "/books/getAllBooks"
                ).permitAll()
                // Read-only endpoints needed by the public home page and reader
                .requestMatchers(HttpMethod.GET,
                    "/books/getBookById/*",
                    "/books/*/pdf",
                    "/books/search",
                    "/uploads/qrcodes/**",
                    "/uploads/barcodes/**"
                ).permitAll()
                .requestMatchers("/submitBook.html").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login.html") // static login page
                .loginProcessingUrl("/login") // POST action for form
                .defaultSuccessUrl("/submitBook.html", true) // after login
                .failureUrl("/login.html?error") // on failure
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/index.html")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(AdminRepository adminRepository) {
        return username -> adminRepository.findByUsername(username)
                .map(admin -> User.builder()
                        .username(admin.getUsername())
                        .password(admin.getPassword())
                        .roles("ADMIN")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    
}
