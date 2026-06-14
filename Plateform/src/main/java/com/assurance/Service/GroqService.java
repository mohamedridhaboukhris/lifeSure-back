


package com.assurance.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GroqService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.api.model}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String askGroq(String userMessage, String contextData) {

        WebClient webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .build();

        String systemPrompt = """
            Tu es l'assistant virtuel de LifeSure, une plateforme d'assurance tunisienne.
            Tu réponds de manière professionnelle, courtoise et concise (max 4-5 phrases).
            Tu peux aider sur : contrats, sinistres, paiements, réclamations, tarifs.
            Si on te demande quelque chose hors assurance, redirige poliment vers le sujet.
            Réponds en français sauf si on te parle dans une autre langue.

            CONTEXTE UTILISATEUR :
            %s
            """.formatted(
                contextData != null ? contextData : "Utilisateur visiteur (non connecté)"
        );

        // Body au format OpenAI (Groq utilise le même format)
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                ),
                "temperature", 0.7,
                "max_tokens", 500
        );

        try {
            System.out.println("🚀 Appel Groq en cours...");
            long startTime = System.currentTimeMillis();

            String response = webClient.post()
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();

            long duration = System.currentTimeMillis() - startTime;
            System.out.println("✅ Groq a répondu en " + duration + " ms");

            // Format de réponse OpenAI : choices[0].message.content
            JsonNode root = objectMapper.readTree(response);
            JsonNode contentNode = root
                    .path("choices").get(0)
                    .path("message")
                    .path("content");

            return contentNode.asText();

        } catch (WebClientResponseException e) {
            System.err.println("❌ Groq API error: " + e.getStatusCode() + " - " + e.getMessage());
            if (e.getStatusCode().value() == 429) {
                return "GROQ_QUOTA_EXCEEDED";
            }
            return "GROQ_API_ERROR";

        } catch (Exception e) {
            System.err.println("❌ Erreur Groq : " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return "GROQ_NETWORK_ERROR";
        }
    }
}
