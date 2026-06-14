package com.assurance.Service;

import com.assurance.Entity.Contrat;
import com.assurance.Entity.StatutContrat;
import com.assurance.Entity.TypeContrat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ContratService {
    Contrat creerContrat(Contrat contrat);
    Contrat getContratById(Long id);
    Contrat getContratByNumero(String numero);
    List<Contrat> getAllContrats();
    List<Contrat> getContratsByClient(Long clientId);
    List<Contrat> getContratsByType(TypeContrat type);
    List<Contrat> getContratsByClientAndStatut(Long clientId, StatutContrat statut);
    List<Contrat> getContratsExpirantEntre(LocalDate start, LocalDate end);
    Map<TypeContrat, Long> statistiquesNombreParType();
    Map<TypeContrat, Double> statistiquesRevenusParType();
    Contrat activerContrat(Long id);
    Contrat suspendreContrat(Long id);
    Contrat resilierContrat(Long id);
    void supprimerContrat(Long id);
    Contrat updateContrat(Long id, Contrat contrat);
}
