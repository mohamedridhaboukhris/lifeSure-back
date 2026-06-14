package com.assurance.Service;

import com.assurance.Entity.Reclamation;
import com.assurance.Entity.StatutReclamation;
//import com.assurance.Repository.DocumentReclamationRepository;
import com.assurance.Entity.User;
import com.assurance.Repository.ReclamationRepository;
import com.assurance.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReclamationServiceImpl implements ReclamationService {
    private final ReclamationRepository reclamationRepository;
//    private final DocumentReclamationRepository documentRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;




    // Soumettre une réclamation
    @Override
    public Reclamation soumettreReclamation(Reclamation reclamation, Long clientId) {

        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client introuvable"));

        reclamation.setClient(client);

        return reclamationRepository.save(reclamation);
    }

    // Liste par client


    @Override
    public List<Reclamation> getReclamationsClient(Long clientId) {
        return reclamationRepository.findByClient_Id(clientId);
    }
    // Traiter une réclamation alli kenet tekhdem maghir mail
//    @Override
//    public Reclamation traiter(Long reclamationId, StatutReclamation statut, String justification) {
//        Reclamation r = reclamationRepository.findById(reclamationId)
//                .orElseThrow(() -> new RuntimeException("Réclamation introuvable"));
//        r.setStatut(statut);
//        r.setJustification(justification);
//        return reclamationRepository.save(r);
//    }




    @Override
    public Reclamation traiter(Long reclamationId, StatutReclamation statut, String justification) {

        Reclamation r = reclamationRepository.findById(reclamationId)
                .orElseThrow(() -> new RuntimeException("Réclamation introuvable"));

        r.setStatut(statut);
        r.setJustification(justification);
        r.setDateDepot(LocalDateTime.now());

        Reclamation saved = reclamationRepository.save(r);

        User client = saved.getClient();

        if (client == null || client.getEmail() == null) {
            throw new RuntimeException("Client email introuvable");
        }

        String emailClient = client.getEmail();

        String subject = "Réponse à votre réclamation #" + saved.getId();

        String message = "Bonjour " + client.getNom() + ",\n\n"
                + "Votre réclamation a été traitée.\n"
                + "Statut : " + statut + "\n"
                + "Justification : " + (justification != null ? justification : "Aucune justification") + "\n\n"
                + "Cordialement,\n"
                + "Plateforme Assurance.";

        emailService.sendEmail(emailClient, subject, message);

        return saved;
    }






















    @Override
    // Toutes les réclamations (admin/expert)
    public List<Reclamation> getAll() {
        return reclamationRepository.findAll();
    }
    @Override
    public List<Reclamation> getReclamationsSoumises() {

        // 1. Récupérer les SOUMISE et les passer EN_COURS
        List<Reclamation> soumises = reclamationRepository.findByStatut(StatutReclamation.SOUMISE);
        for (Reclamation r : soumises) {
            r.setStatut(StatutReclamation.EN_COURS);
        }
        reclamationRepository.saveAll(soumises);

        // 2. Retourner TOUTES les EN_COURS (pas seulement celles qu'on vient de modifier)
        return reclamationRepository.findByStatut(StatutReclamation.EN_COURS);
    }
}

