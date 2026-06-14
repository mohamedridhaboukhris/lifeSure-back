package com.assurance.Repository;

import com.assurance.Entity.Reclamation;
import com.assurance.Entity.StatutReclamation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReclamationRepository extends JpaRepository<Reclamation, Long> {
    List<Reclamation> findByClient_Id(Long clientId);
    List<Reclamation> findByStatut(StatutReclamation statut);
//    Optional<Reclamation> findByNumeroReclamation(String numeroReclamation);
//
//    List<Reclamation> findByClientId(Long clientId);
//
//    List<Reclamation> findByAgentId(Long agentId);
//
//    List<Reclamation> findByStatut(StatutReclamation statut);



}
