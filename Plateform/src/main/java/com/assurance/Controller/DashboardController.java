package com.assurance.Controller;





//import com.assurance.DTO.PredictionRevenusResponse;
import com.assurance.Entity.*;
        import com.assurance.Repository.*;
        import com.assurance.Service.ContratService;
//import com.assurance.Service.PredictionRevenusService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

        import java.math.BigDecimal;
import java.util.*;
        import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ContratRepository contratRepository;
    private final SinistreRepository sinistreRepository;
    private final ReclamationRepository reclamationRepository;
    private final UserRepository userRepository;
    private final ContratService contratService;



    // ============================
    // 📊 DASHBOARD CLIENT
    // ============================
    @GetMapping("/client")
    @PreAuthorize("hasRole('CLIENT')")
    public Map<String, Object> dashboardClient(Authentication auth) {
        String email = auth.getName();
        User client = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Client introuvable"));

        Long clientId = client.getId();

        Map<String, Object> stats = new HashMap<>();

        List<Contrat> contrats = contratRepository.findByClientId(clientId);
        long contratsActifs = contrats.stream()
                .filter(c -> c.getStatut() == StatutContrat.ACTIF).count();

        List<Sinistre> sinistres = sinistreRepository.findByClientId(clientId);
        long sinistresEnCours = sinistres.stream()
                .filter(s -> s.getStatut() == StatutSinistre.DECLARE
                        || s.getStatut() == StatutSinistre.EN_COURS).count();

        BigDecimal totalIndemnisations = sinistres.stream()
                .filter(s -> s.getMontantIndemnisation() != null)
                .map(Sinistre::getMontantIndemnisation)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long reclamationsEnAttente = reclamationRepository.findByClient_Id(clientId).stream()
                .filter(r -> r.getStatut() == StatutReclamation.SOUMISE
                        || r.getStatut() == StatutReclamation.EN_COURS).count();

        stats.put("contratsTotal", contrats.size());
        stats.put("contratsActifs", contratsActifs);
        stats.put("sinistresTotal", sinistres.size());
        stats.put("sinistresEnCours", sinistresEnCours);
        stats.put("totalIndemnisations", totalIndemnisations);
        stats.put("reclamationsEnAttente", reclamationsEnAttente);

        return stats;
    }

    // ============================
    // 📊 DASHBOARD AGENT
    // ============================
//    @GetMapping("/agent")
//    @PreAuthorize("hasRole('AGENT')")
//    public Map<String, Object> dashboardAgent() {
//        Map<String, Object> stats = new HashMap<>();
//
//        List<Contrat> contrats = contratRepository.findAll();
//        long contratsActifs = contrats.stream()
//                .filter(c -> c.getStatut() == StatutContrat.ACTIF).count();
//        long contratsEnAttente = contrats.stream()
//                .filter(c -> c.getStatut() == StatutContrat.EN_ATTENTE).count();
//
//        List<Sinistre> sinistres = sinistreRepository.findAll();
//        long sinistresDeclares = sinistres.stream()
//                .filter(s -> s.getStatut() == StatutSinistre.DECLARE).count();
//        long sinistresEnCours = sinistres.stream()
//                .filter(s -> s.getStatut() == StatutSinistre.EN_COURS).count();
//
//        // Revenus mensuels totaux
//        BigDecimal revenusMensuels = contrats.stream()
//                .filter(c -> c.getStatut() == StatutContrat.ACTIF)
//                .map(Contrat::getPrimeMensuelle)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        // Stats par type
//        Map<TypeContrat, Long> contratsParType = contratService.statistiquesNombreParType();
//        Map<TypeContrat, Double> revenusParType = contratService.statistiquesRevenusParType();
//
//        // Nombre de clients
//        long totalClients = userRepository.findAll().stream()
//                .filter(u -> u.getRole() == Role.CLIENT).count();
//
//        stats.put("contratsTotal", contrats.size());
//        stats.put("contratsActifs", contratsActifs);
//        stats.put("contratsEnAttente", contratsEnAttente);
//        stats.put("sinistresTotal", sinistres.size());
//        stats.put("sinistresDeclares", sinistresDeclares);
//        stats.put("sinistresEnCours", sinistresEnCours);
//        stats.put("revenusMensuels", revenusMensuels);
//        stats.put("contratsParType", contratsParType);
//        stats.put("revenusParType", revenusParType);
//        stats.put("totalClients", totalClients);
//
//        return stats;
//    }



    @GetMapping("/agent")
    @PreAuthorize("hasRole('AGENT')")
    public Map<String, Object> dashboardAgent() {
        Map<String, Object> stats = new HashMap<>();

        List<Contrat> contrats = contratRepository.findAll();
        long contratsActifs = contrats.stream()
                .filter(c -> c.getStatut() == StatutContrat.ACTIF).count();
        long contratsEnAttente = contrats.stream()
                .filter(c -> c.getStatut() == StatutContrat.EN_ATTENTE).count();

        List<Sinistre> sinistres = sinistreRepository.findAll();
        long sinistresDeclares = sinistres.stream()
                .filter(s -> s.getStatut() == StatutSinistre.DECLARE).count();
        long sinistresEnCours = sinistres.stream()
                .filter(s -> s.getStatut() == StatutSinistre.EN_COURS).count();

        BigDecimal revenusMensuels = contrats.stream()
                .filter(c -> c.getStatut() == StatutContrat.ACTIF)
                .map(Contrat::getPrimeMensuelle)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<TypeContrat, Long> contratsParType = contratService.statistiquesNombreParType();
        Map<TypeContrat, Double> revenusParType = contratService.statistiquesRevenusParType();

        long totalClients = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.CLIENT).count();

        // ── TOP CLIENTS ──
        List<Map<String, Object>> topClients = contrats.stream()
                .filter(c -> c.getClient() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getClient(),
                        Collectors.toList()
                ))
                .entrySet().stream()
                .sorted((a, b) -> b.getValue().size() - a.getValue().size())
                .limit(4)
                .map(entry -> {
                    User client = entry.getKey();
                    List<Contrat> clientContrats = entry.getValue();
                    BigDecimal totalPrimes = clientContrats.stream()
                            .map(Contrat::getPrimeMensuelle)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    Map<String, Object> clientMap = new HashMap<>();
                    clientMap.put("nom", client.getNom());
                    clientMap.put("prenom", client.getPrenom());
                    clientMap.put("email", client.getEmail());
                    clientMap.put("nbContrats", clientContrats.size());
                    clientMap.put("totalPrimes", totalPrimes);
                    return clientMap;
                })
                .collect(Collectors.toList());

        stats.put("contratsTotal", contrats.size());
        stats.put("contratsActifs", contratsActifs);
        stats.put("contratsEnAttente", contratsEnAttente);
        stats.put("sinistresTotal", sinistres.size());
        stats.put("sinistresDeclares", sinistresDeclares);
        stats.put("sinistresEnCours", sinistresEnCours);
        stats.put("revenusMensuels", revenusMensuels);
        stats.put("contratsParType", contratsParType);
        stats.put("revenusParType", revenusParType);
        stats.put("totalClients", totalClients);
        stats.put("topClients", topClients);

        return stats;
    }



    // ============================
    // 📊 DASHBOARD EXPERT
    // ============================
    @GetMapping("/expert")
    @PreAuthorize("hasRole('EXPERT')")
    public Map<String, Object> dashboardExpert(Authentication auth) {
        String email = auth.getName();
        User expert = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Expert introuvable"));

        Map<String, Object> stats = new HashMap<>();

        List<Sinistre> sinistresAssignes = sinistreRepository.findByExpertId(expert.getId());

        long enAttente = sinistresAssignes.stream()
                .filter(s -> s.getStatut() == StatutSinistre.EN_COURS).count();
        long acceptes = sinistresAssignes.stream()
                .filter(s -> s.getStatut() == StatutSinistre.ACCEPTE).count();
        long refuses = sinistresAssignes.stream()
                .filter(s -> s.getStatut() == StatutSinistre.REFUSE).count();

        long fraudesDetectees = sinistresAssignes.stream()
                .filter(s -> Boolean.TRUE.equals(s.getFraude())).count();

        long reclamationsEnAttente = reclamationRepository.findByStatut(StatutReclamation.SOUMISE).size()
                + reclamationRepository.findByStatut(StatutReclamation.EN_COURS).size();

        BigDecimal totalIndemnisations = sinistresAssignes.stream()
                .filter(s -> s.getStatut() == StatutSinistre.ACCEPTE && s.getMontantIndemnisation() != null)
                .map(Sinistre::getMontantIndemnisation)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ── TOP SINISTRES SUSPECTS (fraude IA) ──
        List<Map<String, Object>> sinistresSupects = sinistresAssignes.stream()
                .filter(s -> s.getScoreFraude() != null)
                .sorted((a, b) -> Double.compare(
                        b.getScoreFraude().doubleValue(),
                        a.getScoreFraude().doubleValue()))
                .limit(3)
                .map(s -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("numeroSinistre", s.getNumeroSinistre());
                    m.put("scoreFraude", s.getScoreFraude());
                    m.put("fraude", s.getFraude());
                    return m;
                })
                .collect(Collectors.toList());

        stats.put("sinistresTotal", sinistresAssignes.size());
        stats.put("sinistresEnAttente", enAttente);
        stats.put("sinistresAcceptes", acceptes);
        stats.put("sinistresRefuses", refuses);
        stats.put("fraudesDetectees", fraudesDetectees);
        stats.put("reclamationsEnAttente", reclamationsEnAttente);
        stats.put("totalIndemnisations", totalIndemnisations);
        stats.put("sinistresSupects", sinistresSupects);

        return stats;
    }
}