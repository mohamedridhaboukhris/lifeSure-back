/*package com.assurance.Entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "reclamations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reclamation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Le numéro de réclamation est obligatoire")
    @Column(unique = true, nullable = false, length = 50)
    private String numeroReclamation;

    @NotNull(message = "Le type de réclamation est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeReclamation typeReclamation;

    @NotBlank(message = "La description est obligatoire")
    @Column(nullable = false, length = 2000)
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutReclamation statut;


    @Column(length = 2000)
    private String decision;


    @Column(nullable = false)
    private LocalDateTime dateDepot;

    private LocalDateTime dateTraitement;



    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private User agent;

    // Réclamation peut concerner un sinistre OU un contrat
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sinistre_id")
    private Sinistre sinistre;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrat_id")
    private Contrat contrat;

    @OneToMany(mappedBy = "reclamation", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<DocumentReclamation> documents = new ArrayList<>();

}*/


package com.assurance.Entity;

import jakarta.persistence.*;
        import lombok.*;
        import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reclamation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    private String sujet;

    private String description;

    private String justification; // pour la réponse de l'expert

    @Enumerated(EnumType.STRING)
    private StatutReclamation statut;

    private LocalDateTime dateDepot;
    @ManyToOne
    @JoinColumn(name = "client_id")
    private User client;

    @ManyToOne
    @JoinColumn(name = "agent_id")
    private User agent;

    @PrePersist
    protected void onCreate() {
        dateDepot = LocalDateTime.now();
        statut = StatutReclamation.SOUMISE;
    }
}