package com.assurance.Service;

import com.assurance.DTO.PlafondCheckResponse;
import com.assurance.DTO.SinistreDTO;
import com.assurance.Entity.*;
import com.assurance.Repository.ContratRepository;
import com.assurance.Repository.DocumentRepository;
import com.assurance.Repository.SinistreRepository;
import com.assurance.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;

import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SinistreServiceImpl implements SinistreService  {

    private final SinistreRepository sinistreRepository;
    private final UserRepository userRepository;
    private final ContratRepository contratRepository;
    private final EmailService emailService;









    @Override
    public Sinistre declarerSinistre(SinistreDTO dto, Long clientId, List<MultipartFile> fichiers) throws IOException {

        // 🔹 1. Charger le client
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client introuvable"));

        // 🔹 2. Charger le contrat
        Contrat contrat = contratRepository.findByNumeroContrat(dto.getNumeroContrat())
                .orElseThrow(() -> new RuntimeException("Contrat introuvable avec le numéro: " + dto.getNumeroContrat()));




        // 🔹 3. Créer le sinistre
        Sinistre sinistre = Sinistre.builder()
                .dateSinistre(dto.getDateSinistre())
                .description(dto.getDescription())
                .typeSinistre(dto.getTypeSinistre())
                .montantEstime(dto.getMontantEstime())
                .client(client)
                .contrat(contrat)
                .statut(StatutSinistre.DECLARE)
                .dateDeclaration(LocalDateTime.now())
                .numeroSinistre("SIN-" + System.currentTimeMillis())
                // 🗺️ AJOUTÉ — Géolocalisation
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .lieuSinistre(dto.getLieuSinistre())
                .build();












        // 🔹 4. Ajouter documents
        if (fichiers != null) {
            for (MultipartFile file : fichiers) {
                DocumentSinistre doc = new DocumentSinistre();
                doc.setContent(file.getBytes());
                doc.setFileName(file.getOriginalFilename());
                doc.setFileType(file.getContentType());
                doc.setSinistre(sinistre);
                sinistre.getDocuments().add(doc);
            }
        }

        // 🔥 =========================
        // 🔥 5. CALCUL FEATURES IA
        // 🔥 =========================

        int nbSinistres = sinistreRepository.countByClientId(clientId);

        int delai = (int) java.time.temporal.ChronoUnit.DAYS.between(
                sinistre.getDateSinistre(),
                sinistre.getDateDeclaration()
        );

        sinistre.setNbSinistresClient(nbSinistres);
        sinistre.setDelaiDeclaration(delai);

        // 🔥 =========================
        // 🔥 6. APPEL API IA
        // 🔥 =========================

        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:5001/predict";

        Map<String, Object> request = new HashMap<>();
        request.put("montant", sinistre.getMontantEstime());
        request.put("nb_sinistres", nbSinistres);
        request.put("delai", delai);
        request.put("type", sinistre.getTypeSinistre().ordinal());

        try {
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

            double score = ((Number) response.get("score")).doubleValue();
            boolean fraude = score > 0.6;

            sinistre.setScoreFraude(score);
            sinistre.setFraude(fraude);

        } catch (Exception e) {
            // 🔥 si API IA ne marche pas → éviter crash
            sinistre.setScoreFraude(0.0);
            sinistre.setFraude(false);
            System.out.println("Erreur IA : " + e.getMessage());
        }

        // 🔹 7. Sauvegarde
        return sinistreRepository.save(sinistre);
    }
    public int calculerNbSinistres(Long clientId) {
        return sinistreRepository.countByClientId(clientId);
    }

    public int calculerDelai(Sinistre s) {
        return (int) ChronoUnit.DAYS.between(
                s.getDateSinistre(),
                s.getDateDeclaration()
        );
    }


    public Map<String, Object> appelerIA(Sinistre sinistre) {

        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:5001/predict";

        Map<String, Object> request = new HashMap<>();
        request.put("montant", sinistre.getMontantEstime());
        request.put("nb_sinistres", sinistre.getNbSinistresClient());
        request.put("delai", sinistre.getDelaiDeclaration());
        request.put("type", sinistre.getTypeSinistre().ordinal());

        return restTemplate.postForObject(url, request, Map.class);
    }
    @Override
    public List<Sinistre> getSinistresByClient(Long clientId) {
        return sinistreRepository.findByClientId(clientId);
    }

    @Override
    public Sinistre affecterAgent(Long sinistreId, Long agentId) {

        Sinistre sinistre = sinistreRepository.findById(sinistreId)
                .orElseThrow(() -> new RuntimeException("Sinistre introuvable"));

        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent introuvable"));

        sinistre.affecterAgent(agent); // هذا يبدل statut إلى EN_COURS
        return sinistreRepository.save(sinistre);
    }

    public PlafondCheckResponse checkPlafond(Long sinistreId) {

        Sinistre sinistre = sinistreRepository.findById(sinistreId)
                .orElseThrow(() -> new RuntimeException("Sinistre introuvable"));

        Contrat contrat = sinistre.getContrat();

        BigDecimal plafond = null;

        if (contrat.getTypeContrat() == TypeContrat.SANTE) {
            plafond = contrat.getPlafondAnnuel();
        } else if (contrat.getTypeContrat() == TypeContrat.VOYAGE) {
            plafond = contrat.getPlafondAssurance();
        } else {
            return new PlafondCheckResponse(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    "Ce type de contrat ne possède pas de plafond.",
                    false
            );
        }

        BigDecimal montantDejaPaye = sinistreRepository
                .sumMontantIndemnisationByContrat(contrat.getId());

        BigDecimal restant = plafond.subtract(montantDejaPaye);

        if (restant.compareTo(BigDecimal.ZERO) < 0) {
            restant = BigDecimal.ZERO;
        }

        boolean depasse = montantDejaPaye.compareTo(plafond) >= 0;

        String message;

        if (depasse) {
            message = "Plafond dépassé : montant déjà payé (" + montantDejaPaye +
                    " DT) >= plafond (" + plafond + " DT). Refus recommandé.";
        } else {
            message = "Plafond OK : montant déjà payé (" + montantDejaPaye +
                    " DT). Il reste (" + restant + " DT). Vous pouvez affecter un expert.";
        }

        return new PlafondCheckResponse(plafond, montantDejaPaye, restant, message, depasse);
    }

    @Override
    public Sinistre affecterExpert(Long sinistreId, Long expertId) {
        Sinistre sinistre = sinistreRepository.findById(sinistreId)
                .orElseThrow(() -> new RuntimeException("Sinistre introuvable"));

        User expert = userRepository.findById(expertId)
                .orElseThrow(() -> new RuntimeException("Expert introuvable"));

        sinistre.affecterExpert(expert);
        return sinistreRepository.save(sinistre);
    }




    @Override
    public Sinistre refuserSinistre(Long sinistreId) {

        Sinistre sinistre = sinistreRepository.findById(sinistreId)
                .orElseThrow(() -> new RuntimeException("Sinistre introuvable"));

        sinistre.refuser();
        Sinistre saved = sinistreRepository.save(sinistre);

        // 📧 EMAIL CLIENT
        String emailClient = sinistre.getClient().getEmail();

        String subject = "Sinistre refusé #" + sinistre.getNumeroSinistre();

        String message = "Bonjour " + sinistre.getClient().getNom() + ",\n\n"
                + "Votre sinistre a été refusé.\n"
                + "Raison : décision de l’expert.\n\n"
                + "Cordialement,\nAssurance.";

        emailService.sendEmail(emailClient, subject, message);

        return saved;
    }
    @Override
    public List<Sinistre> getSinistresByExpert(Long expertId) {
        return sinistreRepository.findByExpertId(expertId);
    }
    @Override
    public Sinistre cloturerSinistre(Long sinistreId) {
        Sinistre sinistre = sinistreRepository.findById(sinistreId)
                .orElseThrow(() -> new RuntimeException("Sinistre introuvable"));

        sinistre.cloturer();
        return sinistreRepository.save(sinistre);
    }

    @Override
    public Sinistre getSinistreById(Long id) {
        return sinistreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sinistre introuvable"));
    }

    @Override
    public List<Sinistre> getAllSinistres() {
        return sinistreRepository.findAll();
    }
    @Override
    public Sinistre getSinistreByNumero(String numero) {
        return sinistreRepository.findByNumeroSinistre(numero)
                .orElseThrow(() -> new RuntimeException("Sinistre introuvable avec le numéro: " + numero));
    }

    @Override
   public List<Sinistre> getSinistresByStatut(StatutSinistre statut) {
        return sinistreRepository.findByStatut(statut);
    }
@Override
    public List<Sinistre> getSinistresDeclare() {
        return sinistreRepository.findByStatut(StatutSinistre.DECLARE);
    }


    // IA simple pour estimer le montant
    @Override
    public BigDecimal iaEstimerMontant(Sinistre sinistre) {
        if (Boolean.TRUE.equals(sinistre.getFraude())) return BigDecimal.ZERO;

        BigDecimal montant = sinistre.getMontantEstime();
     //   BigDecimal plafond = sinistre.getContrat().getMontantGarantie();






        BigDecimal plafond;

        switch (sinistre.getContrat().getTypeContrat()) {

            case AUTO:
            case HABITATION:
                plafond = sinistre.getContrat().getMontantGarantie();
                break;

            case VOYAGE:
                plafond = sinistre.getContrat().getPlafondAssurance();
                break;

            case SANTE:
                plafond = sinistre.getContrat().getPlafondAnnuel();
                break;

            default:
                plafond = BigDecimal.ZERO; // ولا exception حسب الحاجة
        }










        // Facteur selon type de sinistre
        BigDecimal facteurType = switch (sinistre.getTypeSinistre()) {
            case ACCIDENT -> new BigDecimal("0.95");
            case VOL, VOL_HABITATION -> new BigDecimal("0.9");
            case INCENDIE_HABITATION -> new BigDecimal("0.85");
            default -> BigDecimal.ONE;
        };
        montant = montant.multiply(facteurType);

        // Réduction selon délais
        if (sinistre.getDelaiDeclaration() != null && sinistre.getDelaiDeclaration() > 30) {
            montant = montant.multiply(new BigDecimal("0.9"));
        }

        // Réduction selon historique client
        if (sinistre.getNbSinistresClient() != null && sinistre.getNbSinistresClient() > 3) {
            montant = montant.multiply(new BigDecimal("0.8"));
        }

        // Ne pas dépasser le plafond
        if (montant.compareTo(plafond) > 0) {
            montant = plafond;
        }

        return montant.setScale(2, RoundingMode.HALF_UP);
    }

    // Estimation IA + sauvegarde
    @Override
    public Sinistre estimerAvecIA(Long sinistreId) {
        Sinistre sinistre = sinistreRepository.findById(sinistreId)
                .orElseThrow(() -> new RuntimeException("Sinistre introuvable"));

        BigDecimal estimation = iaEstimerMontant(sinistre);
        sinistre.setMontantIndemnisation(estimation);

        return sinistreRepository.save(sinistre);
    }


public Sinistre accepterSinistre(Long sinistreId, BigDecimal montant) {

    Sinistre sinistre = sinistreRepository.findById(sinistreId)
            .orElseThrow(() -> new RuntimeException("Sinistre introuvable"));

    sinistre.accepter(montant);
    Sinistre saved = sinistreRepository.save(sinistre);

    // 📧 EMAIL CLIENT
    String emailClient = sinistre.getClient().getEmail();

    String subject = "Sinistre accepté #" + sinistre.getNumeroSinistre();

    String message = "Bonjour " + sinistre.getClient().getNom() + ",\n\n"
            + "Votre sinistre a été accepté.\n"
            + "Montant d'indemnisation : " + montant + "\n\n"
            + "Cordialement,\nAssurance.";

    emailService.sendEmail(emailClient, subject, message);

    return saved;
}


}
