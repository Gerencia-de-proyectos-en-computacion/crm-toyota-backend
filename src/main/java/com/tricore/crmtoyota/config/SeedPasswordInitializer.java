package com.tricore.crmtoyota.config;

import com.tricore.crmtoyota.domain.entity.User;
import com.tricore.crmtoyota.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SeedPasswordInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SeedPasswordInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (User user : userRepository.findAll()) {
            if (user.getPasswordHash() != null && user.getPasswordHash().startsWith("SEED:")) {
                String rawPassword = user.getPasswordHash().substring("SEED:".length());
                user.setPasswordHash(passwordEncoder.encode(rawPassword));
                userRepository.save(user);
            }
        }
    }
}
