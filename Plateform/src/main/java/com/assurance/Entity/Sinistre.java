package com.assurance.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sinistres")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sinistre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String numeroSinistre;

    @NotNull(message = "La date du sinistre est obligatoire")
    @Column(nullable = false)
    private LocalDate dateSinistre;

    @NotBlank(message = "La description est obligatoire")
    @Column(nullable = false, length = 1000)
    private String description;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutSinistre statut;

    @Column(precision = 12, scale = 2)
    private BigDecimal montantEstime;

    @Column(precision = 12, scale = 2)
    private BigDecimal montantIndemnisation;


    @Column(nullable = false)
    private LocalDateTime dateDeclaration;
    private LocalDateTime dateCloture;

    @NotNull(message = "Le type de sinistre est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeSinistre typeSinistre;
    @Column
    private Boolean fraude;

    @Column
    private Double scoreFraude;
    @Column
    private Integer nbSinistresClient;

    @Column
    private Integer delaiDeclaration;


    // 🗺️ Géolocalisation du sinistre
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "lieu_sinistre")
    private String lieuSinistre;










    // 🔗 Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "numero_Contrat", nullable = false)
    private Contrat contrat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User agent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User expert;

    // 🔗 Documents
    @Builder.Default
    @OneToMany(mappedBy
            = "sinistre", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentSinistre> documents = new ArrayList<>();







    // Lifecycle
    @PrePersist
    protected void onCreate() {
        dateDeclaration = LocalDateTime.now();
        if (statut == null) statut = StatutSinistre.DECLARE;
        if (numeroSinistre == null) {
            numeroSinistre = "SIN-" + System.currentTimeMillis();
        }
    }

    // Méthodes métier
    public void accepter(BigDecimal montant) {
        this.statut = StatutSinistre.ACCEPTE;
        this.montantIndemnisation = montant;
    }

    public void refuser() {
        this.statut = StatutSinistre.REFUSE;
        this.dateCloture = LocalDateTime.now();
    }

    public void cloturer() {
        this.statut = StatutSinistre.CLOTURE;
        this.dateCloture = LocalDateTime.now();
    }

    public void affecterAgent(User agent) {
        this.agent = agent;
        this.statut = StatutSinistre.EN_COURS;
    }

    public void affecterExpert(User expert) {
        this.expert = expert;
        this.statut = StatutSinistre.EN_COURS;
    }
}
