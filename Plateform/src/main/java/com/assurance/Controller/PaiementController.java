////package com.assurance.Controller;
////
////import com.assurance.Entity.Paiement;
////import com.assurance.Service.PaiementService;
////import lombok.RequiredArgsConstructor;
////import org.springframework.security.access.prepost.PreAuthorize;
////import org.springframework.web.bind.annotation.*;
////
////import java.util.List;
////
////@RestController
////@RequestMapping("/api/paiements")
////@RequiredArgsConstructor
////public class PaiementController {
////
////    private final PaiementService paiementService;
////    @PreAuthorize("hasRole('CLIENT')")
////    @PostMapping("/contrat/{contratId}")
////    public Paiement effectuerPaiement(@PathVariable Long contratId, @RequestBody Paiement paiement) {
////        return paiementService.effectuerPaiement(contratId, paiement);
////    }
////    @PreAuthorize("hasAnyRole('CLIENT','AGENT')")
////    @GetMapping("/contrat/{contratId}")
////    public List<Paiement> getPaiementsByContrat(@PathVariable Long contratId) {
////        return paiementService.getPaiementsByContrat(contratId);
////    }
////}
//
//
//
//
//
//
//
//
//
//package com.assurance.Controller;
//
//import com.assurance.DTO.PaymentRequest;
//import com.assurance.Entity.Contrat;
//import com.assurance.Entity.ModePaiement;
//import com.assurance.Entity.Paiement;
//import com.assurance.Entity.StatutPaiement;
//import com.assurance.Entity.User;
//import com.assurance.Repository.ContratRepository;
//import com.assurance.Repository.UserRepository;
//import com.assurance.Service.PaiementService;
//import com.assurance.Service.StripeService;
//import com.stripe.exception.StripeException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//
//        import java.math.BigDecimal;
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/paiements")
//@RequiredArgsConstructor
//public class PaiementController {
//
//    private final PaiementService paiementService;
//    private final StripeService stripeService;
//    private final ContratRepository contratRepository;
//    private final UserRepository userRepository;
//
//    // 🔵 1. Créer le PaymentIntent (Stripe)
//    @PostMapping("/create-payment-intent")
//    @PreAuthorize("hasRole('CLIENT')")
//    public Map<String, Object> createPaymentIntent(
//            @RequestBody PaymentRequest request,
//            Authentication authentication) throws StripeException {
//
//        String email = authentication.getName();
//
//        Contrat contrat = contratRepository.findById(request.getContratId())
//                .orElseThrow(() -> new RuntimeException("Contrat introuvable"));
//
//        // Sécurité : vérifier que c'est bien le contrat du client
//        if (!contrat.getClient().getEmail().equals(email)) {
//            throw new RuntimeException("Vous n'êtes pas le propriétaire de ce contrat");
//        }
//
//        // Montant en centimes (Stripe demande des centimes)
//        Long montantCentimes = contrat.getPrimeMensuelle()
//                .multiply(BigDecimal.valueOf(100))
//                .longValue();
//
//        return stripeService.createPaymentIntent(montantCentimes, request.getContratId(), email);
//    }
//
//    // 🔵 2. Confirmer et enregistrer le paiement après succès Stripe
//    @PostMapping("/confirmer/{contratId}")
//    @PreAuthorize("hasRole('CLIENT')")
//    public Paiement confirmerPaiement(
//            @PathVariable Long contratId,
//            @RequestParam String paymentIntentId,
//            Authentication authentication) {
//
//        Contrat contrat = contratRepository.findById(contratId)
//                .orElseThrow(() -> new RuntimeException("Contrat introuvable"));
//
//        Paiement paiement = Paiement.builder()
//                .contrat(contrat)
//                .montant(contrat.getPrimeMensuelle())
//                .modePaiement(ModePaiement.CARTE_BANCAIRE)
//                .statut(StatutPaiement.REUSSI)
//                .build();
//
//        return paiementService.effectuerPaiement(contratId, paiement);
//    }
//
//    // 🔵 3. Voir mes paiements
//    @GetMapping("/mes-paiements")
//    @PreAuthorize("hasRole('CLIENT')")
//    public List<Paiement> mesPaiements(Authentication authentication) {
//        String email = authentication.getName();
//        User client = userRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("Client introuvable"));
//        return paiementService.getPaiementsByClient(client.getId());
//    }
//
//    // 🟡 AGENT : voir tous les paiements d'un contrat
//    @GetMapping("/contrat/{contratId}")
//    @PreAuthorize("hasAnyRole('AGENT','CLIENT')")
//    public List<Paiement> getByContrat(@PathVariable Long contratId) {
//        return paiementService.getPaiementsByContrat(contratId);
//    }
//}





























package com.assurance.Controller;

import com.assurance.DTO.PaymentRequest;
import com.assurance.Entity.Contrat;
import com.assurance.Entity.ModePaiement;
import com.assurance.Entity.Notification;
import com.assurance.Entity.Paiement;
import com.assurance.Entity.StatutPaiement;
import com.assurance.Entity.User;
import com.assurance.Repository.ContratRepository;
import com.assurance.Repository.UserRepository;
import com.assurance.Service.NotificationService;
import com.assurance.Service.PaiementService;
import com.assurance.Service.StripeService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/paiements")
@RequiredArgsConstructor
public class PaiementController {

    private final PaiementService paiementService;
    private final StripeService stripeService;
    private final ContratRepository contratRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;  // ✅ AJOUTÉ

    // 🔵 1. Créer le PaymentIntent (Stripe)
    @PostMapping("/create-payment-intent")
    @PreAuthorize("hasRole('CLIENT')")
    public Map<String, Object> createPaymentIntent(
            @RequestBody PaymentRequest request,
            Authentication authentication) throws StripeException {

        String email = authentication.getName();

        Contrat contrat = contratRepository.findById(request.getContratId())
                .orElseThrow(() -> new RuntimeException("Contrat introuvable"));

        if (!contrat.getClient().getEmail().equals(email)) {
            throw new RuntimeException("Vous n'êtes pas le propriétaire de ce contrat");
        }

        Long montantCentimes = contrat.getPrimeMensuelle()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        return stripeService.createPaymentIntent(montantCentimes, request.getContratId(), email);
    }

    // 🔵 2. Confirmer et enregistrer le paiement après succès Stripe
    @PostMapping("/confirmer/{contratId}")
    @PreAuthorize("hasRole('CLIENT')")
    public Paiement confirmerPaiement(
            @PathVariable Long contratId,
            @RequestParam String paymentIntentId,
            Authentication authentication) {

        Contrat contrat = contratRepository.findById(contratId)
                .orElseThrow(() -> new RuntimeException("Contrat introuvable"));

        Paiement paiement = Paiement.builder()
                .contrat(contrat)
                .montant(contrat.getPrimeMensuelle())
                .modePaiement(ModePaiement.CARTE_BANCAIRE)
                .statut(StatutPaiement.REUSSI)
                .build();

        Paiement saved = paiementService.effectuerPaiement(contratId, paiement);

        // 🔔 NOTIFIER TOUS LES AGENTS
        notificationService.notifierTousLesAgents(
                Notification.TypeNotification.PAIEMENT_RECU,
                "💰 Nouveau paiement reçu",
                saved.getMontant() + " DT - Contrat "
                        + saved.getContrat().getNumeroContrat()
                        + " - " + saved.getContrat().getClient().getPrenom()
                        + " " + saved.getContrat().getClient().getNom(),
                "💰",
                "#1cc88a",
                "/admin/paiements"
        );

        return saved;
    }

    // 🔵 3. Voir mes paiements
    @GetMapping("/mes-paiements")
    @PreAuthorize("hasRole('CLIENT')")
    public List<Paiement> mesPaiements(Authentication authentication) {
        String email = authentication.getName();
        User client = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Client introuvable"));
        return paiementService.getPaiementsByClient(client.getId());
    }

    // 🟡 AGENT : voir tous les paiements d'un contrat
    @GetMapping("/contrat/{contratId}")
    @PreAuthorize("hasAnyRole('AGENT','CLIENT')")
    public List<Paiement> getByContrat(@PathVariable Long contratId) {
        return paiementService.getPaiementsByContrat(contratId);
    }
}