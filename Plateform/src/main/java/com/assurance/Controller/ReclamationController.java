//package com.assurance.Controller;
//
//import com.assurance.Entity.Reclamation;
//import com.assurance.Entity.StatutReclamation;
//import com.assurance.Entity.User;
//import com.assurance.Repository.UserRepository;
//import com.assurance.Service.ReclamationService;
//import lombok.RequiredArgsConstructor;
//
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/reclamations")
//@RequiredArgsConstructor
//public class ReclamationController {
//    private final ReclamationService reclamationService;
//    private final UserRepository userRepository;
//
//
//
//    @PostMapping("/soumettre")
//    @PreAuthorize("hasRole('CLIENT')")
//    public Reclamation soumettre(@RequestBody Reclamation reclamation,
//                                 Authentication authentication) {
//
//        String email = authentication.getName();
//
//        User client = userRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
//
//        return reclamationService.soumettreReclamation(reclamation, client.getId());
//    }
//
//
//
//    // Traiter (expert)
//    @PutMapping("/{id}/traiter")
//    @PreAuthorize("hasRole('EXPERT')")
//    public Reclamation traiter(@PathVariable Long id,
//                               @RequestParam StatutReclamation statut,
//                               @RequestParam(required = false) String justification) {
//        return reclamationService.traiter(id, statut, justification);
//    }
//    @GetMapping("/mes-reclamations")
//    @PreAuthorize("hasRole('CLIENT')")
//    public List<Reclamation> mesReclamations(Authentication authentication) {
//
//        String email = authentication.getName();
//
//        User client = userRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
//
//        return reclamationService.getReclamationsClient(client.getId());
//    }
//
//
//    @GetMapping("/soumises")
//    @PreAuthorize("hasRole('EXPERT')")
//    public List<Reclamation> getReclamationsSoumises() {
//        return reclamationService.getReclamationsSoumises();
//    }
//
//}





package com.assurance.Controller;

import com.assurance.Entity.Notification;
import com.assurance.Entity.Reclamation;
import com.assurance.Entity.StatutReclamation;
import com.assurance.Entity.User;
import com.assurance.Repository.UserRepository;
import com.assurance.Service.NotificationService;
import com.assurance.Service.ReclamationService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reclamations")
@RequiredArgsConstructor
public class ReclamationController {

    private final ReclamationService reclamationService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;  // ✅ AJOUTÉ

    @PostMapping("/soumettre")
    @PreAuthorize("hasRole('CLIENT')")
    public Reclamation soumettre(@RequestBody Reclamation reclamation,
                                 Authentication authentication) {

        String email = authentication.getName();

        User client = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Reclamation saved = reclamationService.soumettreReclamation(reclamation, client.getId());

        // 🔔 NOTIFIER TOUS LES AGENTS
        notificationService.notifierTousLesAgents(
                Notification.TypeNotification.RECLAMATION_SOUMISE,
                "📩 Nouvelle réclamation",
                client.getPrenom() + " " + client.getNom()
                        + " a soumis une réclamation : " + saved.getSujet(),
                "📩",
                "#f6c23e",
                "/admin/reclamations-expert"
        );

        return saved;
    }

    // Traiter (expert)
    @PutMapping("/{id}/traiter")
    @PreAuthorize("hasRole('EXPERT')")
    public Reclamation traiter(@PathVariable Long id,
                               @RequestParam StatutReclamation statut,
                               @RequestParam(required = false) String justification) {
        return reclamationService.traiter(id, statut, justification);
    }

    @GetMapping("/mes-reclamations")
    @PreAuthorize("hasRole('CLIENT')")
    public List<Reclamation> mesReclamations(Authentication authentication) {

        String email = authentication.getName();

        User client = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        return reclamationService.getReclamationsClient(client.getId());
    }

    @GetMapping("/soumises")
    @PreAuthorize("hasRole('EXPERT')")
    public List<Reclamation> getReclamationsSoumises() {
        return reclamationService.getReclamationsSoumises();
    }
}