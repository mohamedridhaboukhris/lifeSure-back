//package com.assurance.Service;
//
//import com.assurance.Entity.Contrat;
//import com.assurance.Entity.Paiement;
//import com.assurance.Repository.ContratRepository;
//import com.assurance.Repository.PaiementRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class PaiementServiceImpl implements PaiementService {
//    private final PaiementRepository paiementRepository;
//    private final ContratRepository contratRepository;
//
//    @Override
//    public Paiement effectuerPaiement(Long contratId, Paiement paiement) {
//        Contrat contrat = contratRepository.findById(contratId)
//                .orElseThrow(() -> new RuntimeException("Contrat introuvable"));
//
//        paiement.setContrat(contrat);
//
//        return paiementRepository.save(paiement);
//    }
//
//    @Override
//    public List<Paiement> getPaiementsByContrat(Long contratId) {
//        return paiementRepository.findByContratId(contratId);    }
//}



package com.assurance.Service;

import com.assurance.Entity.Contrat;
import com.assurance.Entity.Paiement;
import com.assurance.Entity.StatutContrat;
import com.assurance.Repository.ContratRepository;
import com.assurance.Repository.PaiementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaiementServiceImpl implements PaiementService {

    private final PaiementRepository paiementRepository;
    private final ContratRepository contratRepository;
    private final EmailService emailService;

    @Override
    public Paiement effectuerPaiement(Long contratId, Paiement paiement) {
        Contrat contrat = contratRepository.findById(contratId)
                .orElseThrow(() -> new RuntimeException("Contrat introuvable"));

        paiement.setContrat(contrat);
        Paiement saved = paiementRepository.save(paiement);

        // ✅ Activer automatiquement le contrat après le 1er paiement réussi
        if (contrat.getStatut() == StatutContrat.EN_ATTENTE) {
            contrat.activer();
            contratRepository.save(contrat);
        }

        // ✅ Envoyer email de confirmation
        if (contrat.getClient() != null && contrat.getClient().getEmail() != null) {
            String subject = "Confirmation de paiement - Contrat " + contrat.getNumeroContrat();
            String message = "Bonjour " + contrat.getClient().getNom() + ",\n\n"
                    + "Votre paiement de " + paiement.getMontant() + " DT a bien été reçu.\n"
                    + "Contrat : " + contrat.getNumeroContrat() + "\n"
                    + "Date : " + paiement.getDatePaiement() + "\n\n"
                    + "Merci de votre confiance.\nLifeSure Assurance";
            emailService.sendEmail(contrat.getClient().getEmail(), subject, message);
        }

        return saved;
    }













    @Override
    public List<Paiement> getPaiementsByContrat(Long contratId) {
        return paiementRepository.findByContratId(contratId);
    }

    @Override
    public List<Paiement> getPaiementsByClient(Long clientId) {
        return paiementRepository.findByContrat_Client_Id(clientId);
    }
}