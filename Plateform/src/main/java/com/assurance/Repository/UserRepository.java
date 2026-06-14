package com.assurance.Repository;

import com.assurance.Entity.Role;
import com.assurance.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository  extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    List<User> findByRole(Role role);

    List<User> findByActif(Boolean actif);

    List<User> findByNomContainingOrPrenomContaining(String nom, String prenom);
}
