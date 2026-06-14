
package com.assurance.DTO;

import lombok.*;

        import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarteSanteResponse {
    private String numeroAdherent;
    private String nomComplet;
    private String email;
    private String telephone;
    private String typeContrat;
    private BigDecimal plafondAnnuel;        // ✅ BigDecimal
    private BigDecimal plafondUtilise;       // ✅ BigDecimal
    private BigDecimal plafondRestant;       // ✅ BigDecimal
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String statut;
    private String urlVerification;
}