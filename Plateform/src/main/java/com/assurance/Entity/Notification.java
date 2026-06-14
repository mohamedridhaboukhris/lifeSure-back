package com.assurance.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinataire_id")
    private User destinataire;



    // Type de notification
    @Enumerated(EnumType.STRING)
    private TypeNotification type;

    // Titre court
    @Column(nullable = false)
    private String titre;

    // Message détaillé
    @Column(length = 1000)
    private String message;

    // Icône (emoji)
    private String icone;

    // Couleur (badge)
    private String couleur;

    // Lien vers la ressource concernée (ex: /admin/contrats/25)
    private String lien;

    // Statut
    private Boolean lue = false;

    // Date création
    private LocalDateTime dateCreation;

    @PrePersist
    public void prePersist() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
        if (lue == null) {
            lue = false;
        }
    }

    public enum TypeNotification {
        CONTRAT_CREE,
        SINISTRE_DECLARE,
        PAIEMENT_RECU,
        RECLAMATION_SOUMISE,
        CONTRAT_SIGNE,
        EXPERT_AFFECTE,

        CONTRAT_VALIDE,       // 🆕
        CONTRAT_REFUSE,       // 🆕
    }
}