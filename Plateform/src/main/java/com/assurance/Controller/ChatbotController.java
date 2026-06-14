package com.assurance.Controller;

import com.assurance.Entity.Contrat;
import com.assurance.Entity.Sinistre;
import com.assurance.Entity.User;
import com.assurance.Repository.ContratRepository;
import com.assurance.Repository.SinistreRepository;
import com.assurance.Repository.UserRepository;
import com.assurance.Service.ChatbotFallback;
import com.assurance.Service.GroqService;  // ✅ CHANGÉ
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final GroqService groqService;  // ✅ CHANGÉ
    private final ChatbotFallback chatbotFallback;
    private final UserRepository userRepository;
    private final ContratRepository contratRepository;
    private final SinistreRepository sinistreRepository;

    @PostMapping("/ask")
    public Map<String, String> ask(@RequestBody Map<String, String> body,
                                   Authentication authentication) {

        String message = body.get("message");
        String context = "Visiteur non connecté";

        User user = null;
        List<Contrat> contrats = null;
        List<Sinistre> sinistres = null;

        if (authentication != null && authentication.isAuthenticated()
                && !authentication.getName().equals("anonymousUser")) {

            String email = authentication.getName();
            user = userRepository.findByEmail(email).orElse(null);

            if (user != null) {
                contrats = contratRepository.findByClientId(user.getId());
                sinistres = sinistreRepository.findByClientId(user.getId());
                context = buildUserContext(user, contrats, sinistres);
            }
        }

        // 1️⃣ Essayer Groq
        String reply = groqService.askGroq(message, context);

        // 2️⃣ Si Groq a échoué → utiliser le fallback
        if (reply.equals("GROQ_QUOTA_EXCEEDED")
                || reply.equals("GROQ_API_ERROR")
                || reply.equals("GROQ_NETWORK_ERROR")) {

            System.out.println("⚠️ Groq indisponible, utilisation du fallback");
            reply = chatbotFallback.getAnswer(message, user, contrats, sinistres);
        }

        Map<String, String> response = new HashMap<>();
        response.put("reply", reply);
        return response;
    }

    private String buildUserContext(User user, List<Contrat> contrats, List<Sinistre> sinistres) {
        StringBuilder ctx = new StringBuilder();
        ctx.append("Utilisateur : ").append(user.getPrenom()).append(" ").append(user.getNom());
        ctx.append("\nEmail : ").append(user.getEmail());
        ctx.append("\nRôle : ").append(user.getRole());

        if (user.getRole().toString().equals("CLIENT")) {
            ctx.append("\n\nSES CONTRATS (").append(contrats.size()).append(") :");
            if (contrats.isEmpty()) {
                ctx.append("\n  Aucun contrat pour le moment.");
            }
            for (Contrat c : contrats) {
                ctx.append(String.format(
                        "\n- %s | Type : %s | Statut : %s | Prime : %s DT | Du %s au %s",
                        c.getNumeroContrat(), c.getTypeContrat(), c.getStatut(),
                        c.getPrimeMensuelle(), c.getDateDebut(), c.getDateFin()
                ));
            }

            ctx.append("\n\nSES SINISTRES (").append(sinistres.size()).append(") :");
            if (sinistres.isEmpty()) {
                ctx.append("\n  Aucun sinistre déclaré.");
            }
            for (Sinistre s : sinistres) {
                ctx.append(String.format(
                        "\n- %s | Type : %s | Statut : %s | Estimé : %s DT | Indemnisation : %s",
                        s.getNumeroSinistre(), s.getTypeSinistre(), s.getStatut(),
                        s.getMontantEstime(),
                        s.getMontantIndemnisation() != null ? s.getMontantIndemnisation() + " DT" : "en attente"
                ));
            }
        }

        return ctx.toString();
    }
}