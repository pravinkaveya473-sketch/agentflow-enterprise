package com.cjl.java_ai_agent.security;

import com.cjl.java_ai_agent.config.AppProperties;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AdminUserInitializer implements ApplicationRunner {
    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final AppProperties properties;

    public AdminUserInitializer(AppUserRepository users, PasswordEncoder encoder, AppProperties properties) {
        this.users = users;
        this.encoder = encoder;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        String username = properties.security().adminUsername();
        if (!users.existsByUsername(username)) {
            users.save(new AppUserEntity(UUID.randomUUID().toString(), username,
                    encoder.encode(properties.security().adminPassword()), Role.ADMIN));
        }
    }
}
