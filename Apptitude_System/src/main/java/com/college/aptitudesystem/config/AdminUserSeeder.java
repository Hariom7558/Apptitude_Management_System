package com.college.aptitudesystem.config;

import com.college.aptitudesystem.entity.AdminUser;
import com.college.aptitudesystem.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserSeeder implements ApplicationRunner {

    private final AdminUserRepository adminUserRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:Admin@123}")
    private String adminPassword;

    public AdminUserSeeder(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (adminUserRepository.findByUsername(adminUsername).isPresent()) {
            return;
        }

        AdminUser adminUser = new AdminUser();
        adminUser.setUsername(adminUsername);
        adminUser.setPasswordHash(passwordEncoder.encode(adminPassword));
        adminUser.setEmail("admin@college.edu");

        adminUserRepository.save(adminUser);
    }
}
