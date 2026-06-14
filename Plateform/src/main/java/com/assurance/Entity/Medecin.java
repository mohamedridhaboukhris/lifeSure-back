

package com.assurance.Entity;

import jakarta.persistence.*;
        import lombok.*;

@Entity
@Table(name = "medecins")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medecin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    private String specialite; // Cardiologie, Dermatologie, Dentiste, etc.

    @Column(nullable = false)
    private String ville;

    @Column(length = 500)
    private String adresse;

    private String telephone;
    private String email;

    private Double tarifNormal;       // Tarif sans assurance
    private Double tarifPartenaire;   // Tarif réduit pour assurés LifeSure

    private Double note;              // Note /5 (ex: 4.5)
    private Integer nbAvis;           // Nombre d'avis

    @Column(length = 1000)
    private String description;

    private String horaires;          // Ex: "Lun-Ven 9h-18h"

    private Boolean actif = true;     // Médecin actif ou non
    private Boolean conventionne = true;  // Conventionné LifeSure
}