package com.assurance.Repository;

import com.assurance.Entity.Contrat;
import com.assurance.Entity.StatutContrat;
import com.assurance.Entity.TypeContrat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ContratRepository extends JpaRepository<Contrat, Long> {
    Optional<Contrat> findByNumeroContrat(String numeroContrat);

    List<Contrat> findByClientId(Long clientId);



    // Recherche par type d'assurance
    List<Contrat> findByTypeContrat(TypeContrat typeContrat);

    // Recherche contrats actifs d'un client
    List<Contrat> findByClientIdAndStatut(Long clientId, StatutContrat statut);



    // Recherche contrats expirant bientôt
    @Query("SELECT c FROM Contrat c WHERE c.dateFin BETWEEN :startDate AND :endDate AND c.statut = 'ACTIF'")
    List<Contrat> findExpiringContracts(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Statistiques - nombre de contrats par type
    @Query("SELECT c.typeContrat, COUNT(c) FROM Contrat c GROUP BY c.typeContrat")
    List<Object[]> countByTypeContrat();

    // Statistiques - revenus par type
    @Query("SELECT c.typeContrat, SUM(c.primeMensuelle) FROM Contrat c WHERE c.statut = 'ACTIF' GROUP BY c.typeContrat")
    List<Object[]> sumPrimeByType();
    List<Contrat> findByStatut(StatutContrat statut);

    // Recherche par client et date de début
    List<Contrat> findByClientIdAndDateDebutAfter(Long clientId, LocalDate startDate);



}
