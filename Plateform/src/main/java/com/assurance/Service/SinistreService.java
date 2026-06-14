package com.assurance.Service;

import com.assurance.DTO.PlafondCheckResponse;
import com.assurance.DTO.SinistreDTO;
import com.assurance.Entity.Sinistre;
import com.assurance.Entity.StatutSinistre;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public interface SinistreService {


    Sinistre declarerSinistre(SinistreDTO dto, Long clientId , List<MultipartFile> fichiers)throws IOException;

    List<Sinistre> getSinistresByClient(Long clientId);
    // AGENT
     Sinistre affecterAgent(Long sinistreId, Long agentId);
     Sinistre affecterExpert(Long sinistreId, Long expertId);
     Sinistre accepterSinistre(Long sinistreId, BigDecimal montant);
     Sinistre refuserSinistre(Long sinistreId);
     Sinistre cloturerSinistre(Long sinistreId);
     // COMMUN
     Sinistre getSinistreById(Long id);
    List<Sinistre> getAllSinistres();



    Sinistre getSinistreByNumero(String numero);

    List<Sinistre> getSinistresByStatut(StatutSinistre statut);

     List<Sinistre> getSinistresDeclare();
    PlafondCheckResponse checkPlafond(Long sinistreId);
    List<Sinistre> getSinistresByExpert(Long expertId);
    BigDecimal iaEstimerMontant(Sinistre sinistre);
    Sinistre estimerAvecIA(Long sinistreId);
}
