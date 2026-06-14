package com.assurance.Controller;

import com.assurance.DTO.EvenementCalendrier;
import com.assurance.Entity.Contrat;
import com.assurance.Entity.Paiement;
import com.assurance.Entity.Rappel;
import com.assurance.Repository.ContratRepository;
import com.assurance.Repository.PaiementRepository;
import com.assurance.Repository.RappelRepository;
import com.assurance.Service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/calendrier")
@RequiredArgsConstructor
public class CalendrierController {

    private final ContratRepository contratRepository;
    private final PaiementRepository paiementRepository;
    private final RappelRepository rappelRepository;
    private final EmailService emailService;

    /**
     * 📅 Récupère tous les événements pour le calendrier
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('AGENT', 'ADMINISTRATEUR')")
    public List<EvenementCalendrier> getEvenements() {
        List<EvenementCalendrier> evenements = new ArrayList<>();

        // 1️⃣ Récupérer toutes les expirations de contrats
        List<Contrat> contrats = contratRepository.findAll();
        for (Contrat c : contrats) {
            if (c.getDateFin() == null) continue;

            // Statut couleur
            String couleur = "#dc3545"; // rouge
            if (c.getDateFin().isBefore(LocalDate.now())) {
                couleur = "#6c757d"; // gris (expiré)
            } else if (c.getDateFin().isAfter(LocalDate.now().plusDays(30))) {
                couleur = "#28a745"; // vert (loin)
            }

            boolean rappelEnvoye = !rappelRepository
                    .findByContratIdAndTypeRappel(c.getId(), Rappel.TypeRappel.EXPIRATION)
                    .isEmpty();

            evenements.add(EvenementCalendrier.builder()
                    .id(c.getId())
                    .type("EXPIRATION")
                    .titre("📅 Expiration - " + c.getNumeroContrat())
                    .date(c.getDateFin())
                    .couleur(rappelEnvoye ? "#ffc107" : couleur)
                    .numeroContrat(c.getNumeroContrat())
                    .typeContrat(c.getTypeContrat().toString())
                    .clientNom(c.getClient() != null ? c.getClient().getPrenom() + " " + c.getClient().getNom() : "")
                    .clientEmail(c.getClient() != null ? c.getClient().getEmail() : "")
                    .montant(c.getPrimeMensuelle())
                    .statut(c.getStatut().toString())
                    .rappelEnvoye(rappelEnvoye)
                    .build());
        }

        // 2️⃣ Récupérer tous les paiements (passés et à venir)
        List<Paiement> paiements = paiementRepository.findAll();
        for (Paiement p : paiements) {
            if (p.getDatePaiement() == null) continue;

            // Statut couleur
            String couleur = "#4e73df"; // bleu (à payer)
            if (p.getStatut() != null && p.getStatut().toString().equals("REUSSI")) {
                couleur = "#28a745"; // vert (payé)
            } else if (p.getDatePaiement().isBefore(LocalDate.now())) {
                couleur = "#dc3545"; // rouge (en retard)
            }

            evenements.add(EvenementCalendrier.builder()
                    .id(p.getId())
                    .type("PAIEMENT")
                    .titre("💰 Paiement - " + p.getMontant() + " DT")
                    .date(p.getDatePaiement())
                    .couleur(couleur)
                    .numeroContrat(p.getContrat() != null ? p.getContrat().getNumeroContrat() : "")
                    .typeContrat(p.getContrat() != null ? p.getContrat().getTypeContrat().toString() : "")
                    .clientNom(p.getContrat() != null && p.getContrat().getClient() != null
                            ? p.getContrat().getClient().getPrenom() + " " + p.getContrat().getClient().getNom() : "")
                    .clientEmail(p.getContrat() != null && p.getContrat().getClient() != null
                            ? p.getContrat().getClient().getEmail() : "")
                    .montant(p.getMontant())
                    .statut(p.getStatut() != null ? p.getStatut().toString() : "")
                    .rappelEnvoye(false)
                    .build());
        }

        return evenements;
    }

    /**
     * 📧 Envoyer un rappel manuel pour un contrat
     */
    @PostMapping("/rappel/{contratId}/{type}")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMINISTRATEUR')")
    public String envoyerRappel(@PathVariable Long contratId, @PathVariable String type) {
        Contrat contrat = contratRepository.findById(contratId)
                .orElseThrow(() -> new RuntimeException("Contrat introuvable"));

        if (contrat.getClient() == null || contrat.getClient().getEmail() == null) {
            return "❌ Email client introuvable";
        }

        String email = contrat.getClient().getEmail();
        String prenom = contrat.getClient().getPrenom();
        String sujet, corps;
        Rappel.TypeRappel typeRappel;

        if (type.equalsIgnoreCase("PAIEMENT")) {
            typeRappel = Rappel.TypeRappel.PAIEMENT;
            sujet = "🔔 Rappel : Paiement de votre prime LifeSure";
            corps = "Bonjour " + prenom + ",\n\n"
                    + "Nous vous rappelons qu'un paiement est à effectuer pour votre contrat "
                    + contrat.getNumeroContrat() + ".\n\n"
                    + "Montant : " + contrat.getPrimeMensuelle() + " DT\n"
                    + "Type : " + contrat.getTypeContrat() + "\n\n"
                    + "Merci de procéder au règlement dans les meilleurs délais.\n\n"
                    + "Cordialement,\n"
                    + "L'équipe LifeSure Assurances";
        } else {
            typeRappel = Rappel.TypeRappel.EXPIRATION;
            sujet = "⚠️ Rappel : Expiration de votre contrat LifeSure";
            corps = "Bonjour " + prenom + ",\n\n"
                    + "Votre contrat " + contrat.getNumeroContrat()
                    + " expire le " + contrat.getDateFin() + ".\n\n"
                    + "Type : " + contrat.getTypeContrat() + "\n"
                    + "Nous vous invitons à le renouveler pour continuer à bénéficier de notre couverture.\n\n"
                    + "Cordialement,\n"
                    + "L'équipe LifeSure Assurances";
        }

        // Envoyer l'email
        try {
            emailService.sendEmail(email, sujet, corps);

            // Sauvegarder le rappel
            Rappel rappel = Rappel.builder()
                    .contrat(contrat)
                    .typeRappel(typeRappel)
                    .dateEnvoi(LocalDateTime.now())
                    .emailDestinataire(email)
                    .sujet(sujet)
                    .envoyeAvecSucces(true)
                    .build();
            rappelRepository.save(rappel);

            return "✅ Rappel envoyé à " + email;
        } catch (Exception e) {
            return "❌ Erreur envoi : " + e.getMessage();
        }
    }
}