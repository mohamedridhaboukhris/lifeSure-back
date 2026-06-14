package com.assurance.Service;

//import com.assurance.DTO.IaAnalyseResponse;
import com.assurance.DTO.IaAnalyseResponse;
import com.assurance.Entity.DocumentSinistre;
import com.assurance.Entity.Sinistre;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
public class HuggingFaceService {

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.api.url}")
    private String groqApiUrl;

    @Value("${groq.api.model}")
    private String groqModel;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Analyse un sinistre avec Groq AI (Llama 3.3 70B)
     * UNIQUEMENT pour les sinistres de type AUTO
     */

    public IaAnalyseResponse analyserSinistre(Sinistre sinistre) {

        // ✅ SÉCURITÉ : Vérifier que c'est un sinistre AUTO
        if (sinistre.getContrat() == null
                || sinistre.getContrat().getTypeContrat() == null
                || !sinistre.getContrat().getTypeContrat().toString().equals("AUTO")) {

            System.out.println("⚠️ Analyse IA refusée : le sinistre n'est pas de type AUTO");

            return IaAnalyseResponse.builder()
                    .success(false)
                    .message("L'analyse IA est disponible uniquement pour les sinistres de type AUTO")
                    .build();
        }

        List<DocumentSinistre> documents = sinistre.getDocuments();
        int nbImages = 0;
        if (documents != null) {
            nbImages = (int) documents.stream()
                    .filter(d -> d.getFileType() != null && d.getFileType().startsWith("image/"))
                    .count();
        }

        try {
            // Construire le prompt avec toutes les infos du sinistre
            String prompt = construirePrompt(sinistre, nbImages);

            // Appeler Groq
            String response = appellerGroq(prompt);

            // Parser la réponse JSON
            return parserReponseGroq(response, nbImages);

        } catch (Exception e) {
            System.err.println("❌ Erreur Groq : " + e.getMessage());
            e.printStackTrace();
            return analyseFallback(sinistre, nbImages);
        }
    }

    /**
     * Construit le prompt à envoyer à Groq
     */

    private String construirePrompt(Sinistre sinistre, int nbImages) {
        StringBuilder ctx = new StringBuilder();

        ctx.append("Tu es un expert en assurance automobile qui analyse les sinistres déclarés.\n");
        ctx.append("Voici les informations du sinistre AUTO :\n\n");

        ctx.append("📋 INFORMATIONS DU SINISTRE :\n");
        ctx.append("- Type : ").append(sinistre.getTypeSinistre()).append("\n");
        ctx.append("- Description du client : \"").append(sinistre.getDescription()).append("\"\n");
        ctx.append("- Date du sinistre : ").append(sinistre.getDateSinistre()).append("\n");
        ctx.append("- Montant estimé par client : ").append(sinistre.getMontantEstime()).append(" DT\n");
        ctx.append("- Nombre de photos jointes du véhicule : ").append(nbImages).append("\n");
        ctx.append("- Délai de déclaration : ").append(sinistre.getDelaiDeclaration()).append(" jour(s)\n");

        if (sinistre.getNbSinistresClient() != null) {
            ctx.append("- Sinistres antérieurs du client : ").append(sinistre.getNbSinistresClient()).append("\n");
        }

        if (sinistre.getContrat() != null) {
            ctx.append("\n📄 CONTRAT AUTO :\n");
            ctx.append("- Numéro contrat : ").append(sinistre.getContrat().getNumeroContrat()).append("\n");
            if (sinistre.getContrat().getPrimeMensuelle() != null) {
                ctx.append("- Prime mensuelle : ").append(sinistre.getContrat().getPrimeMensuelle()).append(" DT\n");
            }
        }

        ctx.append("\n");
        ctx.append("🎯 TÂCHE : Analyse ce sinistre AUTO et retourne UNIQUEMENT un JSON structuré (sans texte avant ou après) avec exactement ces champs :\n");
        ctx.append("{\n");
        ctx.append("  \"typeDegat\": \"string (ex: DÉGÂT CARROSSERIE, PARE-CHOC ENDOMMAGÉ, VITRE BRISÉE, COLLISION FRONTALE, VÉHICULE TOTALEMENT DÉTRUIT, VOL DE VÉHICULE, etc.)\",\n");
        ctx.append("  \"gravite\": \"MINEUR | MOYEN | MAJEUR\",\n");
        ctx.append("  \"scoreConfiance\": 0.85,\n");
        ctx.append("  \"montantMin\": 1000,\n");
        ctx.append("  \"montantMax\": 3000,\n");
        ctx.append("  \"montantSuggere\": 2000,\n");
        ctx.append("  \"detailsTechniques\": [\n");
        ctx.append("    \"• Premier point d'analyse\",\n");
        ctx.append("    \"• Deuxième point\",\n");
        ctx.append("    \"• Troisième point\"\n");
        ctx.append("  ]\n");
        ctx.append("}\n\n");
        ctx.append("Règles d'estimation des montants en DT (dinar tunisien) pour AUTO :\n");
        ctx.append("- MINEUR : 200 - 800 DT (rayures, petits chocs)\n");
        ctx.append("- MOYEN : 1000 - 3000 DT (pare-choc, phare, vitre)\n");
        ctx.append("- MAJEUR : 3000 - 10000 DT (collision majeure, incendie, vol)\n\n");
        ctx.append("scoreConfiance entre 0.70 et 0.95 selon la précision de l'analyse.\n");
        ctx.append("Retourne UNIQUEMENT le JSON, sans markdown, sans backticks, sans explication.");

        return ctx.toString();
    }

    /**
     * Appel à Groq AI
     */
    private String appellerGroq(String prompt) throws Exception {
        WebClient webClient = WebClient.builder()
                .baseUrl(groqApiUrl)
                .build();

        Map<String, Object> requestBody = Map.of(
                "model", groqModel,
                "messages", List.of(
                        Map.of("role", "system", "content", "Tu es un expert en assurance automobile. Tu réponds toujours en JSON structuré uniquement, sans markdown."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.3,
                "max_tokens", 800
        );

        System.out.println("🚀 Appel Groq IA pour analyse sinistre AUTO...");
        long start = System.currentTimeMillis();

        String response = webClient.post()
                .header("Authorization", "Bearer " + groqApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(20))
                .block();

        long duration = System.currentTimeMillis() - start;
        System.out.println("✅ Groq a répondu en " + duration + " ms");

        return response;
    }

    /**
     * Parser la réponse JSON de Groq
     */
    private IaAnalyseResponse parserReponseGroq(String response, int nbImages) throws Exception {
        JsonNode root = objectMapper.readTree(response);

        // Extraire le contenu de la réponse Groq
        String content = root
                .path("choices").get(0)
                .path("message")
                .path("content")
                .asText();

        System.out.println("📦 Contenu Groq : " + content);

        // Nettoyer le contenu (enlever ```json si présent)
        content = content.trim();
        if (content.startsWith("```json")) {
            content = content.substring(7);
        }
        if (content.startsWith("```")) {
            content = content.substring(3);
        }
        if (content.endsWith("```")) {
            content = content.substring(0, content.length() - 3);
        }
        content = content.trim();

        // Parser le JSON retourné par Groq
        JsonNode analyseJson = objectMapper.readTree(content);

        // Extraire les détails techniques
        List<String> details = new ArrayList<>();
        JsonNode detailsNode = analyseJson.path("detailsTechniques");
        if (detailsNode.isArray()) {
            for (JsonNode d : detailsNode) {
                details.add(d.asText());
            }
        }

        return IaAnalyseResponse.builder()
                .success(true)
                .typeDegat(analyseJson.path("typeDegat").asText("DÉGÂT VÉHICULE"))
                .gravite(analyseJson.path("gravite").asText("MOYEN"))
                .scoreConfiance(analyseJson.path("scoreConfiance").asDouble(0.80))
                .montantMin(analyseJson.path("montantMin").asDouble(1000))
                .montantMax(analyseJson.path("montantMax").asDouble(3000))
                .montantSuggere(analyseJson.path("montantSuggere").asDouble(2000))
                .detailsTechniques(details)
                .nbImagesAnalysees(nbImages)
                .message("Analyse IA réussie - Groq AI (Llama 3.3 70B)")
                .build();
    }

    /**
     * FALLBACK : analyse intelligente sans IA (si Groq plante)
     */
    private IaAnalyseResponse analyseFallback(Sinistre sinistre, int nbImages) {
        System.out.println("⚠️ Groq indisponible, utilisation du fallback");

        String gravite = "MOYEN";
        String typeDegat = "DÉGÂT VÉHICULE";

        if (sinistre.getTypeSinistre() != null) {
            String type = sinistre.getTypeSinistre().toString();
            if (type.contains("INCENDIE") || type.contains("VOL") || type.contains("DESTRUCTION")) {
                gravite = "MAJEUR";
                typeDegat = type;
            } else if (type.contains("ACCIDENT") || type.contains("DEGAT")) {
                gravite = "MOYEN";
                typeDegat = type;
            } else {
                gravite = "MINEUR";
            }
        }

        double[] montants = calculerMontant(gravite);

        return IaAnalyseResponse.builder()
                .success(true)
                .typeDegat(typeDegat)
                .gravite(gravite)
                .scoreConfiance(0.65)
                .montantMin(montants[0])
                .montantMax(montants[1])
                .montantSuggere(montants[2])
                .detailsTechniques(List.of(
                        "• Analyse basée sur le type de sinistre",
                        "• " + nbImages + " photo(s) du véhicule jointe(s)",
                        "• Mode dégradé activé"
                ))
                .nbImagesAnalysees(nbImages)
                .message("Analyse en mode dégradé")
                .build();
    }

    private double[] calculerMontant(String gravite) {
        return switch (gravite) {
            case "MAJEUR" -> new double[]{3000, 8000, 5000};
            case "MOYEN"  -> new double[]{1000, 3000, 2000};
            case "MINEUR" -> new double[]{200, 800, 500};
            default       -> new double[]{500, 2000, 1000};
        };
    }
}