package com.assurance.Repository;

import com.assurance.Entity.TauxAssurance;
import com.assurance.Entity.TypeContrat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TauxAssuranceRepository extends JpaRepository<TauxAssurance, Long> {
    Optional<TauxAssurance> findByTypeContratAndActifTrue(TypeContrat typeContrat);
}
