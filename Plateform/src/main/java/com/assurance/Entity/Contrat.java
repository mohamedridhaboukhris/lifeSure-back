package com.assurance.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "contrats")
@Data
@NoArgsConstructor
@AllArgsConstructor

@Builder
public class Contrat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column
    private String numeroContrat;


    @NotNull(message = "Le type de contrat est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeContrat typeContrat;

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutContrat statut;

    @NotNull(message = "La date de début est obligatoire")
    @Column(nullable = false)
    private LocalDate dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    @Column(nullable = false)
    private LocalDate dateFin;


    @NotNull
    @Positive(message = "La prime mensuelle doit être positive")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal primeMensuelle;

  // @NotNull(message = "Le montant de garantie est obligatoire")
    @Positive(message = "Le montant de garantie doit être positif")
    @Column
    private BigDecimal montantGarantie;


    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDate dateCreation;

    @Column
    private LocalDate dateModification;
    @Column(nullable = false)
    private Boolean actif = true;


    @JsonIgnore
    @OneToMany(mappedBy = "contrat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Paiement> paiements;




    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User client;











    // Champs spécifiques selon le type de contrat
    @Column(length = 100)
    private String vehiculeMarque; // Pour AUTO

    @Column(length = 100)
    private String vehiculeModele; // Pour AUTO

    @Column(length = 50)
    private String vehiculeImmatriculation; // Pour AUTO

    @Column(length = 255)
    private String adresseBien; // Pour HABITATION

    @Column
    private Integer superficieBien; // Pour HABITATION (m²)

    @Column(length = 100)
    private String typeBien; // Pour HABITATION (appartement, maison...)


    // SANTE
    @Column
    private Integer ageAssure;

    @Column(precision = 10, scale = 2)
    private BigDecimal plafondAnnuel;




    // Pour VOYAGE
    @Column(length = 100)
    private String destination; // Destination du voyage

    @Column
    private Integer dureeVoyage; // Durée du voyage en jours

    // Plafond annuel fixé uniquement par l'agent
    @Column(precision = 10, scale = 2)
    private BigDecimal plafondAssurance; // Montant maximal couvert pour le voyage





    // ✍️ Signature électronique
    @Column(columnDefinition = "LONGTEXT")
    private String signature;  // Base64 de l'image PNG

    @Column(name = "date_signature")
    private LocalDateTime dateSignature;



    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDate.now();
        dateModification = LocalDate.now();
        if (statut == null) {
            statut = StatutContrat.EN_ATTENTE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDate.now();
    }

    // Méthodes métier
    public boolean isExpired() {
        return LocalDate.now().isAfter(dateFin);
    }

    public boolean isActive() {
        return statut == StatutContrat.ACTIF && !isExpired();
    }

    public void suspendre() {
        if (statut == StatutContrat.ACTIF) {
            this.statut = StatutContrat.SUSPENDU;
        }
    }

    public void resilier() {
        this.statut = StatutContrat.RESILIE;
    }

    public void activer() {
        if (statut == StatutContrat.EN_ATTENTE || statut == StatutContrat.SUSPENDU) {
            this.statut = StatutContrat.ACTIF;
        }
    }













///hedi alla zedtha jdiida /////////////////////////

    // 🆕 VALIDATION AGENT
    @Column(length = 1000)
    private String motifRefus;  // Raison du refus (si REFUSE)

    @Column
    private LocalDateTime dateValidation;  // Date acceptation/refus

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_validateur_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User agentValidateur;  // Qui a validé/refusé
////////////////////////////////////////////////////









}
