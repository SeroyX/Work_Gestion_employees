package com.entreprise.gestion.config;

import com.entreprise.gestion.repository.UserRepository;
import com.entreprise.gestion.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Initialise les données de démarrage :
 * - Crée le compte admin par défaut si inexistant.
 *   username : admin@entreprise.com
 *   password : admin123
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        String adminEmail = "admin@entreprise.com";

        if (!userRepository.existsByUsername(adminEmail)) {
            userService.createUser(adminEmail, "admin123", "ROLE_ADMIN");
            log.info("✅ Compte admin créé : {} / admin123", adminEmail);
        } else {
            log.info("ℹ️  Compte admin déjà existant : {}", adminEmail);
        }
    }
}
