package com.assurance.DTO;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvenementCalendrier {
    private Long id;
    private String type;          // PAIEMENT, EXPIRATION
    private String titre;
    private LocalDate date;
    private String couleur;       // #4e73df, #dc3545, #28a745, #ffc107
    private String numeroContrat;
    private String typeContrat;
    private String clientNom;
    private String clientEmail;
    private BigDecimal montant;
    private String statut;
    private Boolean rappelEnvoye;
}