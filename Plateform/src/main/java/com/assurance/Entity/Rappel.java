package com.assurance.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rappels")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rappel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrat_id")
    private Contrat contrat;

    @Enumerated(EnumType.STRING)
    private TypeRappel typeRappel;  // PAIEMENT, EXPIRATION

    private LocalDateTime dateEnvoi;
    private String emailDestinataire;

    @Column(length = 500)
    private String sujet;

    private Boolean envoyeAvecSucces = true;

    public enum TypeRappel {
        PAIEMENT,    // Rappel de paiement de prime
        EXPIRATION   // Rappel d'expiration de contrat
    }
}