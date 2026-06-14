package com.assurance.Repository;

import com.assurance.DTO.SinistreDTO;
import com.assurance.Entity.Contrat;
import com.assurance.Entity.Sinistre;
import com.assurance.Entity.StatutSinistre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SinistreRepository extends JpaRepository<Sinistre, Long> {

    @Query("SELECT COALESCE(SUM(s.montantIndemnisation),0) FROM Sinistre s " +
            "WHERE s.contrat.id = :contratId AND s.statut IN ('ACCEPTE','CLOTURE')")
    BigDecimal sumMontantIndemnisationByContrat(@Param("contratId") Long contratId);
    int countByClientId(Long clientId);

    List<Sinistre> findByFraudeTrue();
    // Récupérer tous les sinistres assignés à un expert
    List<Sinistre> findByExpertId(Long expertId);

    Optional<Sinistre> findByNumeroSinistre(String numeroSinistre);

    List<Sinistre> findByClientId(Long clientId);


    List<Sinistre> findByStatut(StatutSinistre statut);
    List<Sinistre> findByContratId(Long contratId);


    List<Sinistre> findByDateSinistreBetween(LocalDate start, LocalDate end);
}
