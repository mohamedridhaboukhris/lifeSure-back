package com.assurance.Service;

import com.assurance.Entity.Contrat;
import com.assurance.Entity.Paiement;
import com.assurance.Entity.Rappel;
import com.assurance.Repository.ContratRepository;
import com.assurance.Repository.PaiementRepository;
import com.assurance.Repository.RappelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RappelScheduler {

    private final ContratRepository contratRepository;
    private final PaiementRepository paiementRepository;
    private final RappelRepository rappelRepository;
    private final EmailService emailService;

    /**
     * ⏰ Tâche planifiée : tous les jours à 9h00
     * Envoie les rappels d'expiration (30 jours avant)
     * Envoie les rappels de paiement (7 jours avant)
     */
    @Scheduled(cron = "0 0 9 * * *")  // tous les jours à 9h00
    public void envoyerRappelsAutomatiques() {
        System.out.println("🔄 [SCHEDULER] Démarrage des rappels automatiques...");

        rappelsExpiration();
        rappelsPaiement();

        System.out.println("✅ [SCHEDULER] Rappels terminés");
    }

    /**
     * 📅 Rappels d'expiration : 30 jours avant
     */
    private void rappelsExpiration() {
        LocalDate dateLimite = LocalDate.now().plusDays(30);

        List<Contrat> contrats = contratRepository.findAll();
        int compteur = 0;

        for (Contrat contrat : contrats) {
            if (contrat.getDateFin() == null) continue;
            if (contrat.getClient() == null || contrat.getClient().getEmail() == null) continue;

            // Si la date d'expiration est exactement dans 30 jours
            if (contrat.getDateFin().equals(dateLimite)) {
                // Vérifier qu'on n'a pas déjà envoyé un rappel
                boolean dejaEnvoye = !rappelRepository
                        .findByContratIdAndTypeRappel(contrat.getId(), Rappel.TypeRappel.EXPIRATION)
                        .isEmpty();

                if (!dejaEnvoye) {
                    envoyerRappelExpiration(contrat);
                    compteur++;
                }
            }
        }

        System.out.println("   📧 " + compteur + " rappel(s) d'expiration envoyé(s)");
    }

    /**
     * 💰 Rappels de paiement : 7 jours avant
     */
    private void rappelsPaiement() {
        LocalDate dateLimite = LocalDate.now().plusDays(7);

        List<Paiement> paiements = paiementRepository.findAll();
        int compteur = 0;

        for (Paiement paiement : paiements) {
            if (paiement.getDatePaiement() == null) continue;
            if (paiement.getContrat() == null || paiement.getContrat().getClient() == null) continue;

            // Si paiement à venir dans 7 jours et pas encore payé
            if (paiement.getDatePaiement().equals(dateLimite)
                    && !paiement.getStatut().toString().equals("REUSSI")) {

                boolean dejaEnvoye = !rappelRepository
                        .findByContratIdAndTypeRappel(paiement.getContrat().getId(), Rappel.TypeRappel.PAIEMENT)
                        .isEmpty();

                if (!dejaEnvoye) {
                    envoyerRappelPaiement(paiement);
                    compteur++;
                }
            }
        }

        System.out.println("   💰 " + compteur + " rappel(s) de paiement envoyé(s)");
    }

    private void envoyerRappelExpiration(Contrat contrat) {
        try {
            String sujet = "⚠️ Rappel : Expiration de votre contrat LifeSure";
            String corps = "Bonjour " + contrat.getClient().getPrenom() + ",\n\n"
                    + "Votre contrat " + contrat.getNumeroContrat()
                    + " expire le " + contrat.getDateFin() + " (dans 30 jours).\n\n"
                    + "Pensez à le renouveler pour continuer à bénéficier de notre couverture.\n\n"
                    + "Cordialement,\nL'équipe LifeSure Assurances";

            emailService.sendEmail(contrat.getClient().getEmail(), sujet, corps);

            // Sauvegarder
            rappelRepository.save(Rappel.builder()
                    .contrat(contrat)
                    .typeRappel(Rappel.TypeRappel.EXPIRATION)
                    .dateEnvoi(LocalDateTime.now())
                    .emailDestinataire(contrat.getClient().getEmail())
                    .sujet(sujet)
                    .envoyeAvecSucces(true)
                    .build());
        } catch (Exception e) {
            System.err.println("❌ Erreur rappel expiration contrat " + contrat.getNumeroContrat() + " : " + e.getMessage());
        }
    }

    private void envoyerRappelPaiement(Paiement paiement) {
        try {
            String sujet = "🔔 Rappel : Paiement de votre prime LifeSure";
            String corps = "Bonjour " + paiement.getContrat().getClient().getPrenom() + ",\n\n"
                    + "Nous vous rappelons qu'un paiement est à effectuer dans 7 jours.\n\n"
                    + "Contrat : " + paiement.getContrat().getNumeroContrat() + "\n"
                    + "Montant : " + paiement.getMontant() + " DT\n"
                    + "Date d'échéance : " + paiement.getDatePaiement() + "\n\n"
                    + "Merci de procéder au règlement.\n\n"
                    + "Cordialement,\nL'équipe LifeSure Assurances";

            emailService.sendEmail(paiement.getContrat().getClient().getEmail(), sujet, corps);

            rappelRepository.save(Rappel.builder()
                    .contrat(paiement.getContrat())
                    .typeRappel(Rappel.TypeRappel.PAIEMENT)
                    .dateEnvoi(LocalDateTime.now())
                    .emailDestinataire(paiement.getContrat().getClient().getEmail())
                    .sujet(sujet)
                    .envoyeAvecSucces(true)
                    .build());
        } catch (Exception e) {
            System.err.println("❌ Erreur rappel paiement contrat " + paiement.getContrat().getNumeroContrat() + " : " + e.getMessage());
        }
    }
}