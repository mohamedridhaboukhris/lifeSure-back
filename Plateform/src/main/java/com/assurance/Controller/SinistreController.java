//package com.assurance.Controller;
//
//import com.assurance.DTO.IaAnalyseResponse;
//import com.assurance.DTO.PlafondCheckResponse;
//import com.assurance.DTO.SinistreDTO;
//import com.assurance.Entity.DocumentSinistre;
//import com.assurance.Entity.Sinistre;
//import com.assurance.Entity.StatutSinistre;
//import com.assurance.Entity.User;
//import com.assurance.Repository.SinistreRepository;
//import com.assurance.Repository.UserRepository;
//import com.assurance.Service.DocumentSinistreService;
////import com.assurance.Service.GeminiVisionService;
//import com.assurance.Service.HuggingFaceService;
//import com.assurance.Service.SinistreService;
//import com.assurance.Service.SinistreServiceImpl;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.MediaType;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//
//import java.io.IOException;
//import java.math.BigDecimal;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/sinistres")
//@RequiredArgsConstructor
//public class SinistreController {
//    private final SinistreService sinistreService;
//
//    private final DocumentSinistreService documentSinistreService;
//    private final UserRepository userRepository;
//
//    private final HuggingFaceService huggingFaceService;
//    private final SinistreRepository sinistreRepository;
//
//
//
//
//
//
//    @PostMapping(value = "/declarer", consumes = {"multipart/form-data"})
//    public Sinistre declarerSinistre(
//            @RequestPart("data") SinistreDTO dto,
//            @RequestPart(value = "fichiers", required = false) List<MultipartFile> fichiers,
//            Authentication authentication) throws IOException {
//
//        String username = authentication.getName();
//
//        User client = userRepository.findByEmail(username)
//                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
//        return sinistreService.declarerSinistre(dto,client.getId(), fichiers);
//    }
//
//
//
//
//
//
//    @GetMapping("/mes-sinistres-client")
//    @PreAuthorize("hasRole('CLIENT')")
//    public List<Sinistre> getMesSinistresClient(Authentication authentication) {
//        String email = authentication.getName();
//        User client = userRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("Client introuvable"));
//        return sinistreService.getSinistresByClient(client.getId());
//    }
//
//
//
//
//
//
//
//
//    @GetMapping("/declares")
//    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','AGENT')")
//    public List<Sinistre> getSinistresDeclares() {
//        return sinistreService.getSinistresDeclare();
//    }
//    @GetMapping("/client/{clientId}")
//    @PreAuthorize("hasRole('CLIENT')")
//    public List<Sinistre> getSinistresByClient(@PathVariable Long clientId) {
//        return sinistreService.getSinistresByClient(clientId);
//    }
//
//
//    @PutMapping("/{sinistreId}/affecter-agent")
//    @PreAuthorize("hasRole('AGENT')")
//    public Sinistre affecterAgent(@PathVariable Long sinistreId, Authentication authentication) {
//
//        String email = authentication.getName(); // agent connecté
//
//        User agent = userRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("Agent introuvable"));
//
//        return sinistreService.affecterAgent(sinistreId, agent.getId());
//    }
//
//    @PutMapping("/{sinistreId}/affecter-expert/{expertId}")
//    @PreAuthorize("hasRole('AGENT')")
//    public Sinistre affecterExpert(@PathVariable Long sinistreId,@PathVariable Long expertId) {
//        return sinistreService.affecterExpert(sinistreId, expertId);
//    }
//
//   /* @PutMapping("/{sinistreId}/accepter")
//    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','EXPERT','AGENT')")
//    public Sinistre accepterSinistre(@PathVariable Long sinistreId, @RequestParam BigDecimal montant) {
//        return sinistreService.accepterSinistre(sinistreId, montant);
//    }
//*/
//    @PutMapping("/{sinistreId}/refuser")
//    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','EXPERT','AGENT')")
//    public Sinistre refuserSinistre(@PathVariable Long sinistreId) {
//        return sinistreService.refuserSinistre(sinistreId);
//    }
//
//    @PutMapping("/{sinistreId}/cloturer")
//    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','AGENT')")
//    public Sinistre cloturerSinistre(@PathVariable Long sinistreId) {
//        return sinistreService.cloturerSinistre(sinistreId);
//    }
//
//    @GetMapping("/{id}")
//    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','AGENT','CLIENT')")
//    public Sinistre getSinistreById(@PathVariable Long id) {
//        return sinistreService.getSinistreById(id);
//    }
//
//
//
//
//    @GetMapping
//    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','AGENT')")
//    public List<Sinistre> getAllSinistres() {
//        return sinistreService.getAllSinistres();
//    }
//
//
//
//    @PostMapping(
//            value = "/{sinistreId}/documents",
//            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
//    )
//    @PreAuthorize("hasRole('CLIENT')")
//    public Sinistre ajouterDocuments(
//            @PathVariable Long sinistreId,
//            @RequestParam("files") List<MultipartFile> files) {
//
//        if (files == null || files.isEmpty()) {
//            throw new RuntimeException("Aucun fichier reçu");
//        }
//
//        Sinistre sinistre = sinistreService.getSinistreById(sinistreId);
//        documentSinistreService.ajouterDocuments(sinistre, files);
//        return sinistreService.getSinistreById(sinistreId);
//    }
//
//
//    @GetMapping("/{sinistreId}/check-plafond")
//    @PreAuthorize("hasRole('AGENT')")
//    public PlafondCheckResponse checkPlafond(@PathVariable Long sinistreId) {
//        return sinistreService.checkPlafond(sinistreId);
//    }
//
//
//    @GetMapping("/{sinistreId}/documents")
//    @PreAuthorize("hasAnyRole('CLIENT','AGENT','EXPERT','ADMINISTRATEUR')")
//    public List<DocumentSinistre> getDocumentsSinistre(@PathVariable Long sinistreId) {
//        Sinistre sinistre = sinistreService.getSinistreById(sinistreId);
//        return sinistre.getDocuments();
//    }
//
//    // ✅ Rechercher par numéro de sinistre
//    @GetMapping("/numero/{numero}")
//    @PreAuthorize("hasAnyRole('CLIENT','AGENT','EXPERT','ADMINISTRATEUR')")
//    public Sinistre getSinistreByNumero(@PathVariable String numero) {
//        return sinistreService.getSinistreByNumero(numero);
//    }
//
//    // ✅ Filtrer par statut
//    @GetMapping("/statut/{statut}")
//    @PreAuthorize("hasAnyRole('AGENT','ADMINISTRATEUR')")
//    public List<Sinistre> getSinistresByStatut(@PathVariable StatutSinistre statut) {
//        return sinistreService.getSinistresByStatut(statut);
//    }
//
//// expert bech ichouf les sinistres mte3ou
//    @GetMapping("/mes-sinistres")
//    @PreAuthorize("hasRole('EXPERT')")
//    public List<Sinistre> getMesSinistres(Authentication authentication) {
//        // Récupérer le nom d'utilisateur (ou email) depuis le token
//        String email = authentication.getName();
//
//        // Ensuite, récupérer l'expertId depuis la base (repository User)
//        User expert = userRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("Expert introuvable"));
//
//        return sinistreService.getSinistresByExpert(expert.getId());
//    }
//
//
//
//    // Estimer avec IA
//    @PutMapping("/{sinistreId}/estimer")
//    @PreAuthorize("hasRole('EXPERT')")
//    public Sinistre estimerAvecIA(@PathVariable Long sinistreId) {
//        return sinistreService.estimerAvecIA(sinistreId);
//    }
//
//    // Accepter avec ou sans modification
//    @PutMapping("/{sinistreId}/accepter")
//    @PreAuthorize("hasRole('EXPERT')")
//    public Sinistre accepterSinistre(@PathVariable Long sinistreId,
//                                     @RequestParam BigDecimal montant) {
//        return sinistreService.accepterSinistre(sinistreId, montant);
//    }
//
//    // 🤖 Analyse IA d'un sinistre par l'EXPERT
//    @PostMapping("/{id}/analyse-ia")
//    @PreAuthorize("hasRole('EXPERT')")
//    public IaAnalyseResponse analyserAvecIa(@PathVariable Long id) {
//        Sinistre sinistre = sinistreRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Sinistre introuvable"));
//
//        return huggingFaceService.analyserSinistre(sinistre);
//    }
//
//
//
//    // 🗺️ Carte des sinistres - retourne uniquement les infos GPS
//    @GetMapping("/carte")
//    @PreAuthorize("hasAnyRole('AGENT', 'EXPERT')")
//    public List<Map<String, Object>> getSinistresCarte() {
//        return sinistreRepository.findAll().stream()
//                .filter(s -> s.getLatitude() != null && s.getLongitude() != null)
//                .map(s -> {
//                    Map<String, Object> map = new HashMap<>();
//                    map.put("id", s.getId());
//                    map.put("numeroSinistre", s.getNumeroSinistre());
//                    map.put("typeSinistre", s.getTypeSinistre() != null ? s.getTypeSinistre().toString() : "");
//                    map.put("statut", s.getStatut() != null ? s.getStatut().toString() : "");
//                    map.put("latitude", s.getLatitude());
//                    map.put("longitude", s.getLongitude());
//                    map.put("lieuSinistre", s.getLieuSinistre());
//                    map.put("dateSinistre", s.getDateSinistre());
//                    map.put("description", s.getDescription());
//                    map.put("montantEstime", s.getMontantEstime());
//
//                    if (s.getClient() != null) {
//                        map.put("clientNom", s.getClient().getPrenom() + " " + s.getClient().getNom());
//                    }
//                    if (s.getContrat() != null) {
//                        map.put("numeroContrat", s.getContrat().getNumeroContrat());
//                    }
//                    return map;
//                })
//                .collect(Collectors.toList());
//    }
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//    }











package com.assurance.Controller;

import com.assurance.DTO.IaAnalyseResponse;
import com.assurance.DTO.PlafondCheckResponse;
import com.assurance.DTO.SinistreDTO;
import com.assurance.Entity.DocumentSinistre;
import com.assurance.Entity.Notification;
import com.assurance.Entity.Sinistre;
import com.assurance.Entity.StatutSinistre;
import com.assurance.Entity.User;
import com.assurance.Repository.SinistreRepository;
import com.assurance.Repository.UserRepository;
import com.assurance.Service.DocumentSinistreService;
import com.assurance.Service.HuggingFaceService;
import com.assurance.Service.NotificationService;
import com.assurance.Service.SinistreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sinistres")
@RequiredArgsConstructor
public class SinistreController {

    private final SinistreService sinistreService;
    private final DocumentSinistreService documentSinistreService;
    private final UserRepository userRepository;
    private final HuggingFaceService huggingFaceService;
    private final SinistreRepository sinistreRepository;
    private final NotificationService notificationService;  // ✅ AJOUTÉ

    @PostMapping(value = "/declarer", consumes = {"multipart/form-data"})
    public Sinistre declarerSinistre(
            @RequestPart("data") SinistreDTO dto,
            @RequestPart(value = "fichiers", required = false) List<MultipartFile> fichiers,
            Authentication authentication) throws IOException {

        String username = authentication.getName();

        User client = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Sinistre saved = sinistreService.declarerSinistre(dto, client.getId(), fichiers);

        // 🔔 NOTIFIER TOUS LES AGENTS
        notificationService.notifierTousLesAgents(
                Notification.TypeNotification.SINISTRE_DECLARE,
                "🚨 Nouveau sinistre déclaré",
                client.getPrenom() + " " + client.getNom()
                        + " a déclaré un sinistre " + saved.getTypeSinistre()
                        + " (" + saved.getNumeroSinistre() + ")",
                "🚨",
                "#dc3545",
                "/admin/sinistres-agent"
        );

        return saved;
    }

    @GetMapping("/mes-sinistres-client")
    @PreAuthorize("hasRole('CLIENT')")
    public List<Sinistre> getMesSinistresClient(Authentication authentication) {
        String email = authentication.getName();
        User client = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Client introuvable"));
        return sinistreService.getSinistresByClient(client.getId());
    }

    @GetMapping("/declares")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','AGENT')")
    public List<Sinistre> getSinistresDeclares() {
        return sinistreService.getSinistresDeclare();
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasRole('CLIENT')")
    public List<Sinistre> getSinistresByClient(@PathVariable Long clientId) {
        return sinistreService.getSinistresByClient(clientId);
    }

    @PutMapping("/{sinistreId}/affecter-agent")
    @PreAuthorize("hasRole('AGENT')")
    public Sinistre affecterAgent(@PathVariable Long sinistreId, Authentication authentication) {
        String email = authentication.getName();
        User agent = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Agent introuvable"));
        return sinistreService.affecterAgent(sinistreId, agent.getId());
    }

    @PutMapping("/{sinistreId}/affecter-expert/{expertId}")
    @PreAuthorize("hasRole('AGENT')")
    public Sinistre affecterExpert(@PathVariable Long sinistreId, @PathVariable Long expertId) {
        Sinistre sinistre = sinistreService.affecterExpert(sinistreId, expertId);

        // 🔔 NOTIFIER L'EXPERT AFFECTÉ
        User expert = userRepository.findById(expertId).orElse(null);
        if (expert != null) {
            notificationService.notifierUser(
                    expert,
                    Notification.TypeNotification.EXPERT_AFFECTE,
                    "👨‍⚖️ Nouveau sinistre à expertiser",
                    "Le sinistre " + sinistre.getNumeroSinistre()
                            + " (" + sinistre.getTypeSinistre() + ") vous a été affecté",
                    "👨‍⚖️",
                    "#4e73df",
                    "/admin/sinistres-expert"
            );
        }

        return sinistre;
    }

    @PutMapping("/{sinistreId}/refuser")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','EXPERT','AGENT')")
    public Sinistre refuserSinistre(@PathVariable Long sinistreId) {
        return sinistreService.refuserSinistre(sinistreId);
    }

    @PutMapping("/{sinistreId}/cloturer")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','AGENT')")
    public Sinistre cloturerSinistre(@PathVariable Long sinistreId) {
        return sinistreService.cloturerSinistre(sinistreId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','AGENT','CLIENT')")
    public Sinistre getSinistreById(@PathVariable Long id) {
        return sinistreService.getSinistreById(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','AGENT')")
    public List<Sinistre> getAllSinistres() {
        return sinistreService.getAllSinistres();
    }

    @PostMapping(
            value = "/{sinistreId}/documents",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('CLIENT')")
    public Sinistre ajouterDocuments(
            @PathVariable Long sinistreId,
            @RequestParam("files") List<MultipartFile> files) {

        if (files == null || files.isEmpty()) {
            throw new RuntimeException("Aucun fichier reçu");
        }

        Sinistre sinistre = sinistreService.getSinistreById(sinistreId);
        documentSinistreService.ajouterDocuments(sinistre, files);
        return sinistreService.getSinistreById(sinistreId);
    }

    @GetMapping("/{sinistreId}/check-plafond")
    @PreAuthorize("hasRole('AGENT')")
    public PlafondCheckResponse checkPlafond(@PathVariable Long sinistreId) {
        return sinistreService.checkPlafond(sinistreId);
    }

    @GetMapping("/{sinistreId}/documents")
    @PreAuthorize("hasAnyRole('CLIENT','AGENT','EXPERT','ADMINISTRATEUR')")
    public List<DocumentSinistre> getDocumentsSinistre(@PathVariable Long sinistreId) {
        Sinistre sinistre = sinistreService.getSinistreById(sinistreId);
        return sinistre.getDocuments();
    }

    @GetMapping("/numero/{numero}")
    @PreAuthorize("hasAnyRole('CLIENT','AGENT','EXPERT','ADMINISTRATEUR')")
    public Sinistre getSinistreByNumero(@PathVariable String numero) {
        return sinistreService.getSinistreByNumero(numero);
    }

    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasAnyRole('AGENT','ADMINISTRATEUR')")
    public List<Sinistre> getSinistresByStatut(@PathVariable StatutSinistre statut) {
        return sinistreService.getSinistresByStatut(statut);
    }

    @GetMapping("/mes-sinistres")
    @PreAuthorize("hasRole('EXPERT')")
    public List<Sinistre> getMesSinistres(Authentication authentication) {
        String email = authentication.getName();
        User expert = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Expert introuvable"));
        return sinistreService.getSinistresByExpert(expert.getId());
    }

    @PutMapping("/{sinistreId}/estimer")
    @PreAuthorize("hasRole('EXPERT')")
    public Sinistre estimerAvecIA(@PathVariable Long sinistreId) {
        return sinistreService.estimerAvecIA(sinistreId);
    }

    @PutMapping("/{sinistreId}/accepter")
    @PreAuthorize("hasRole('EXPERT')")
    public Sinistre accepterSinistre(@PathVariable Long sinistreId,
                                     @RequestParam BigDecimal montant) {
        return sinistreService.accepterSinistre(sinistreId, montant);
    }

    @PostMapping("/{id}/analyse-ia")
    @PreAuthorize("hasRole('EXPERT')")
    public IaAnalyseResponse analyserAvecIa(@PathVariable Long id) {
        Sinistre sinistre = sinistreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sinistre introuvable"));
        return huggingFaceService.analyserSinistre(sinistre);
    }

    @GetMapping("/carte")
    @PreAuthorize("hasAnyRole('AGENT', 'EXPERT')")
    public List<Map<String, Object>> getSinistresCarte() {
        return sinistreRepository.findAll().stream()
                .filter(s -> s.getLatitude() != null && s.getLongitude() != null)
                .map(s -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", s.getId());
                    map.put("numeroSinistre", s.getNumeroSinistre());
                    map.put("typeSinistre", s.getTypeSinistre() != null ? s.getTypeSinistre().toString() : "");
                    map.put("statut", s.getStatut() != null ? s.getStatut().toString() : "");
                    map.put("latitude", s.getLatitude());
                    map.put("longitude", s.getLongitude());
                    map.put("lieuSinistre", s.getLieuSinistre());
                    map.put("dateSinistre", s.getDateSinistre());
                    map.put("description", s.getDescription());
                    map.put("montantEstime", s.getMontantEstime());

                    if (s.getClient() != null) {
                        map.put("clientNom", s.getClient().getPrenom() + " " + s.getClient().getNom());
                    }
                    if (s.getContrat() != null) {
                        map.put("numeroContrat", s.getContrat().getNumeroContrat());
                    }
                    return map;
                })
                .collect(Collectors.toList());
    }
}