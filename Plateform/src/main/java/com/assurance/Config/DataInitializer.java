package com.assurance.Config;

import com.assurance.Entity.Role;
import com.assurance.Entity.User;
import com.assurance.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Initialisation des données de test...");

            // Créer un administrateur
            User admin = User.builder()
                    .nom("Admin")
                    .prenom("System")
                    .email("admin@assurance.com")
                    .password(passwordEncoder.encode("admin123"))
                    .telephone("+21612345678")
                    .role(Role.ADMINISTRATEUR)
                    .actif(true)
                    .build();

            // Créer un agent
            User agent = User.builder()
                    .nom("Dupont")
                    .prenom("Jean")
                    .email("agent@assurance.com")
                    .password(passwordEncoder.encode("agent123"))
                    .telephone("+21698765432")
                    .role(Role.AGENT)
                    .actif(true)
                    .build();

            // Créer un expert
            User expert = User.builder()
                    .nom("Martin")
                    .prenom("Sophie")
                    .email("expert@assurance.com")
                    .password(passwordEncoder.encode("expert123"))
                    .telephone("+21654321098")
                    .role(Role.EXPERT)
                    .actif(true)
                    .build();

            // Créer un client
            User client = User.builder()
                    .nom("Benali")
                    .prenom("Ahmed")
                    .email("client@assurance.com")
                    .password(passwordEncoder.encode("client123"))
                    .telephone("+21623456789")
                    .role(Role.CLIENT)
                    .actif(true)
                    .build();

            userRepository.save(admin);
            userRepository.save(agent);
            userRepository.save(expert);
            userRepository.save(client);

            log.info("Données de test créées avec succès!");
            log.info("Admin: admin@assurance.com / admin123");
            log.info("Agent: agent@assurance.com / agent123");
            log.info("Expert: expert@assurance.com / expert123");
            log.info("Client: client@assurance.com / client123");
        }
    }
}
