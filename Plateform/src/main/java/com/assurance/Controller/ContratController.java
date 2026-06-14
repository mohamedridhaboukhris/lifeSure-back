package com.assurance.Controller;

import com.assurance.DTO.CarteSanteResponse;
import com.assurance.DTO.SignatureRequest;
import com.assurance.Entity.*;
import com.assurance.Repository.ContratRepository;
import com.assurance.Service.ContratEmailTemplates;
import com.assurance.Service.EmailServiceImpl;
import com.assurance.Service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import com.assurance.Repository.UserRepository;
import com.assurance.Service.ContratService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.assurance.Entity.Notification;





@RestController
@RequestMapping("/api/contrats")
@RequiredArgsConstructor
public class ContratController {
    private final ContratService contratService ;
    private final UserRepository userRepository;
    private final ContratRepository contratRepository;
    private final NotificationService notificationService;
    private final EmailServiceImpl emailService;


//    @PreAuthorize("hasRole('CLIENT')")
//    @PostMapping
//    public Contrat creerContrat(@RequestBody Contrat contrat, Authentication authentication) {
//
//        String username = authentication.getName();
//
//        User client = userRepository.findByEmail(username)
//                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
//
//        contrat.setClient(client);
//
//        Contrat saved = contratService.creerContrat(contrat);
//
//        // 🔔 NOTIFIER TOUS LES AGENTS
//        notificationService.notifierTousLesAgents(
//                Notification.TypeNotification.CONTRAT_CREE,
//                "🟢 Nouveau contrat créé",
//                saved.getClient().getPrenom() + " " + saved.getClient().getNom()
//                        + " a créé un contrat " + saved.getTypeContrat()
//                        + " (" + saved.getNumeroContrat() + ")",
//                "📋",
//                "#28a745",
//                "/admin/contrats"
//        );
//
//        return saved;
//    }


    // Dans createContrat, modifie pour envoyer email au client :
    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping
    public Contrat creerContrat(@RequestBody Contrat contrat, Authentication authentication) {

        String username = authentication.getName();
        User client = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        contrat.setClient(client);

        // 🆕 Forcer statut EN_ATTENTE_VALIDATION
        contrat.setStatut(StatutContrat.EN_ATTENTE_VALIDATION);

        Contrat saved = contratService.creerContrat(contrat);

        // 🔔 NOTIFIER TOUS LES AGENTS
        notificationService.notifierTousLesAgents(
                Notification.TypeNotification.CONTRAT_CREE,
                "🟢 Nouveau contrat à valider",
                saved.getClient().getPrenom() + " " + saved.getClient().getNom()
                        + " a soumis un contrat " + saved.getTypeContrat()
                        + " (" + saved.getNumeroContrat() + ")",
                "📋",
                "#f59e0b",
                "/agent/contrats-en-attente"
        );

        // 📧 EMAIL AU CLIENT
        emailService.sendHtmlEmail(
                client.getEmail(),
                "⏳ Votre demande de contrat est en cours d'examen - " + saved.getNumeroContrat(),
                ContratEmailTemplates.contratSoumis(saved)
        );

        return saved;
    }

    // 🆕 VALIDER UN CONTRAT
    @PreAuthorize("hasRole('AGENT')")
    @PutMapping("/{id}/valider")
    public Contrat validerContrat(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        User agent = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Agent introuvable"));

        Contrat contrat = contratRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrat introuvable"));

        if (contrat.getStatut() != StatutContrat.EN_ATTENTE_VALIDATION) {
            throw new RuntimeException("Ce contrat n'est pas en attente de validation");
        }

        // Mettre à jour
        contrat.setStatut(StatutContrat.EN_ATTENTE);  // Attente paiement maintenant
        contrat.setAgentValidateur(agent);
        contrat.setDateValidation(LocalDateTime.now());

        Contrat saved = contratRepository.save(contrat);

        // 🔔 Notification au client
        notificationService.notifierUser(
                saved.getClient(),
                Notification.TypeNotification.CONTRAT_VALIDE,
                "✅ Contrat accepté !",
                "Votre contrat " + saved.getNumeroContrat() + " a été accepté. Procédez au paiement.",
                "✅",
                "#10b981",
                "/mes-contrats"
        );

        // 📧 Email au client
        emailService.sendHtmlEmail(
                saved.getClient().getEmail(),
                "✅ Votre contrat " + saved.getNumeroContrat() + " a été accepté !",
                ContratEmailTemplates.contratAccepte(saved)
        );

        return saved;
    }

    // 🆕 REFUSER UN CONTRAT
    @PreAuthorize("hasRole('AGENT')")
    @PutMapping("/{id}/refuser")
    public Contrat refuserContrat(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        String motif = body.get("motif");
        if (motif == null || motif.trim().length() < 20) {
            throw new RuntimeException("Le motif doit faire au moins 20 caractères");
        }

        String email = authentication.getName();
        User agent = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Agent introuvable"));

        Contrat contrat = contratRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrat introuvable"));

        if (contrat.getStatut() != StatutContrat.EN_ATTENTE_VALIDATION) {
            throw new RuntimeException("Ce contrat n'est pas en attente de validation");
        }

        contrat.setStatut(StatutContrat.ANNULE);
        contrat.setMotifRefus(motif);
        contrat.setAgentValidateur(agent);
        contrat.setDateValidation(LocalDateTime.now());

        Contrat saved = contratRepository.save(contrat);

        // 🔔 Notification client
        notificationService.notifierUser(
                saved.getClient(),
                Notification.TypeNotification.CONTRAT_REFUSE,
                "❌ Contrat refusé",
                "Votre contrat " + saved.getNumeroContrat() + " a été refusé. Voir détails.",
                "❌",
                "#ef4444",
                "/mes-contrats"
        );

        // 📧 Email client
        emailService.sendHtmlEmail(
                saved.getClient().getEmail(),
                "❌ Décision concernant votre contrat " + saved.getNumeroContrat(),
                ContratEmailTemplates.contratRefuse(saved)
        );

        return saved;
    }

    // 🆕 GET contrats en attente (pour AGENT)
    @PreAuthorize("hasRole('AGENT')")
    @GetMapping("/en-attente-validation")
    public List<Contrat> getContratsEnAttenteValidation() {
        return contratRepository.findByStatut(StatutContrat.EN_ATTENTE_VALIDATION);
    }















































    @PreAuthorize("hasAnyRole('CLIENT','AGENT')")
    @GetMapping("/{id}")
    public Contrat getContratById(@PathVariable Long id) {
        return contratService.getContratById(id);
    }


    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/mes-contrats")
    public List<Contrat> getMesContrats(Authentication authentication) {
        String email = authentication.getName();
        User client = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        return contratService.getContratsByClient(client.getId());
    }




    @PreAuthorize("hasAnyRole('CLIENT','AGENT')")
    @GetMapping("/numero/{numero}")
    public Contrat getContratByNumero(@PathVariable String numero) {
        return contratService.getContratByNumero(numero);
    }

    @PreAuthorize("hasRole('AGENT')")
    @GetMapping
    public List<Contrat> getAllContrats() {
        return contratService.getAllContrats();
    }

    @PreAuthorize("hasRole('AGENT')")
    @GetMapping("/client/{clientId}")
    public List<Contrat> getByClient(@PathVariable Long clientId) {
        return contratService.getContratsByClient(clientId);
    }



    @PreAuthorize("hasRole('AGENT')")
    @GetMapping("/type/{type}")
    public List<Contrat> getByType(@PathVariable TypeContrat type) {
        return contratService.getContratsByType(type);
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/client/{clientId}/statut/{statut}")
    public List<Contrat> getByClientAndStatut(@PathVariable Long clientId,
                                              @PathVariable StatutContrat statut) {
        return contratService.getContratsByClientAndStatut(clientId, statut);
    }

    @PreAuthorize("hasRole('AGENT')")
    @GetMapping("/expiring")
    public List<Contrat> getExpirant(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return contratService.getContratsExpirantEntre(start, end);
    }

    @PreAuthorize("hasRole('AGENT')")
    @GetMapping("/statistiques/nombre")
    public Map<TypeContrat, Long> statistiquesNombreParType() {
        return contratService.statistiquesNombreParType();
    }

    @PreAuthorize("hasRole('AGENT')")
    @GetMapping("/statistiques/revenus")
    public Map<TypeContrat, Double> statistiquesRevenusParType() {
        return contratService.statistiquesRevenusParType();
    }

    @PreAuthorize("hasRole('AGENT')")
    @PutMapping("/{id}/activer")
    public Contrat activerContrat(@PathVariable Long id) {
        return contratService.activerContrat(id);
    }

    @PreAuthorize("hasRole('AGENT')")
    @PutMapping("/{id}/suspendre")
    public Contrat suspendreContrat(@PathVariable Long id) {
        return contratService.suspendreContrat(id);
    }

    @PreAuthorize("hasRole('AGENT')")
    @PutMapping("/{id}/resilier")
    public Contrat resilierContrat(@PathVariable Long id) {
        return contratService.resilierContrat(id);
    }

    @PreAuthorize("hasRole('AGENT')")
    @DeleteMapping("/{id}")
    public void supprimerContrat(@PathVariable Long id) {
        contratService.supprimerContrat(id);
    }

    @PreAuthorize("hasRole('AGENT')")
    @PutMapping("/{id}")
    public Contrat updateContrat(@PathVariable Long id, @RequestBody Contrat contrat) {
        return contratService.updateContrat(id, contrat);
    }




    // ✍️ Signer électroniquement un contrat
    @PostMapping("/{id}/signer")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Map<String, Object>> signerContrat(
            @PathVariable Long id,
            @RequestBody SignatureRequest request,
            Authentication authentication) {

        Contrat contrat = contratRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrat introuvable"));

        // Vérifier que c'est bien le propriétaire du contrat
        String email = authentication.getName();
        if (contrat.getClient() == null || !contrat.getClient().getEmail().equals(email)) {
            return ResponseEntity.status(403).body(Map.of("error", "Vous n'êtes pas autorisé à signer ce contrat"));
        }

        // Vérifier que le contrat n'est pas déjà signé
        if (contrat.getSignature() != null && !contrat.getSignature().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ce contrat est déjà signé"));
        }

        // Vérifier qu'une signature est bien fournie
        if (request.getSignature() == null || request.getSignature().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Signature manquante"));
        }

        // Enregistrer
        contrat.setSignature(request.getSignature());
        contrat.setDateSignature(LocalDateTime.now());
        contratRepository.save(contrat);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Contrat signé avec succès");
        response.put("dateSignature", contrat.getDateSignature());

        return ResponseEntity.ok(response);
    }



    // 💳 Récupérer la carte santé d'un contrat
    @GetMapping("/{id}/carte-sante")
    @PreAuthorize("hasRole('CLIENT')")
    public CarteSanteResponse getCarteSante(@PathVariable Long id, Authentication authentication) {
        Contrat contrat = contratRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrat introuvable"));

        // Vérifier que c'est bien le client propriétaire
        if (!contrat.getClient().getEmail().equals(authentication.getName())) {
            throw new RuntimeException("Non autorisé");
        }

        // Vérifier que c'est un contrat SANTÉ
        if (!contrat.getTypeContrat().toString().equals("SANTE")) {
            throw new RuntimeException("La carte santé est disponible uniquement pour les contrats SANTÉ");
        }

        // ✅ Utilisation de BigDecimal
        BigDecimal plafond = contrat.getPlafondAnnuel() != null
                ? contrat.getPlafondAnnuel()
                : new BigDecimal("5000");

        // Pour l'instant, on simule avec 35% utilisé
        BigDecimal plafondUtilise = plafond.multiply(new BigDecimal("0.35"))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal plafondRestant = plafond.subtract(plafondUtilise);

        return CarteSanteResponse.builder()
                .numeroAdherent(contrat.getNumeroContrat())
                .nomComplet(contrat.getClient().getPrenom() + " " + contrat.getClient().getNom())
                .email(contrat.getClient().getEmail())
                .telephone(contrat.getClient().getTelephone())
                .typeContrat(contrat.getTypeContrat().toString())
                .plafondAnnuel(plafond)
                .plafondUtilise(plafondUtilise)
                .plafondRestant(plafondRestant)
                .dateDebut(contrat.getDateDebut())
                .dateFin(contrat.getDateFin())
                .statut(contrat.getStatut().toString())
                .urlVerification("http://localhost:4200/verifier-contrat/" + contrat.getNumeroContrat())
                .build();
    }







}
