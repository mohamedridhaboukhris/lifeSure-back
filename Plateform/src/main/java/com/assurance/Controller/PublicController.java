package com.assurance.Controller;

import com.assurance.Entity.Contrat;
import com.assurance.Repository.ContratRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

        import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final ContratRepository contratRepository;

    /**
     * Endpoint PUBLIC : vérifie l'authenticité d'un contrat via son numéro
     * Utilisé par les QR codes des attestations
     */
    @GetMapping("/contrat/{numero}")
    public ResponseEntity<Map<String, Object>> verifierContrat(@PathVariable String numero) {

        Map<String, Object> response = new HashMap<>();

        Contrat contrat = contratRepository.findByNumeroContrat(numero).orElse(null);

        if (contrat == null) {
            response.put("valide", false);
            response.put("message", "Contrat introuvable ou attestation falsifiée");
            return ResponseEntity.ok(response);
        }

        // Retourner uniquement les infos PUBLIQUES (pas d'email, pas de mot de passe...)
        response.put("valide", true);
        response.put("numeroContrat", contrat.getNumeroContrat());
        response.put("typeContrat", contrat.getTypeContrat().toString());
        response.put("statut", contrat.getStatut().toString());
        response.put("dateDebut", contrat.getDateDebut());
        response.put("dateFin", contrat.getDateFin());
        response.put("primeMensuelle", contrat.getPrimeMensuelle());

        // Infos client (juste prénom + initiale du nom pour la confidentialité)
        if (contrat.getClient() != null) {
            String prenom = contrat.getClient().getPrenom();
            String nom = contrat.getClient().getNom();
            String initiale = (nom != null && !nom.isEmpty()) ? nom.charAt(0) + "." : "";
            response.put("clientNom", prenom + " " + initiale);
        }

        // Compagnie
        response.put("compagnie", "LifeSure Assurances");

        return ResponseEntity.ok(response);
    }
}
