

package com.assurance.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IaAnalyseResponse {

    private boolean success;
    private String typeDegat;          // CARROSSERIE, VITRE, PARE-CHOC...
    private String gravite;            // MINEUR, MOYEN, MAJEUR
    private double scoreConfiance;     // 0.0 à 1.0 (ex: 0.87)
    private double montantMin;         // Estimation min (DT)
    private double montantMax;         // Estimation max (DT)
    private double montantSuggere;     // Montant suggéré (DT)
    private List<String> detailsTechniques;  // Liste des observations
    private String message;            // Message d'erreur ou succès
    private int nbImagesAnalysees;     // Nombre d'images analysées
}