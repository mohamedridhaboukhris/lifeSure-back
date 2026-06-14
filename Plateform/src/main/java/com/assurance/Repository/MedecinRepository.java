


package com.assurance.Repository;

import com.assurance.Entity.Medecin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedecinRepository extends JpaRepository<Medecin, Long> {
    List<Medecin> findByActifTrue();
    List<Medecin> findBySpecialite(String specialite);
    List<Medecin> findByVille(String ville);
}