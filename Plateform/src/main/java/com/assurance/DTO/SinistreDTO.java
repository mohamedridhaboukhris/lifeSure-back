package com.assurance.DTO;


import com.assurance.Entity.TypeSinistre;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class SinistreDTO {
    @NotNull(message = "La date du sinistre est obligatoire")
    private LocalDate dateSinistre;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    @NotNull(message = "Le type de sinistre est obligatoire")
    private TypeSinistre typeSinistre;

    private BigDecimal montantEstime;

    @NotNull(message = "num  du contrat est obligatoire")
    private String numeroContrat;



    // 🗺️ Géolocalisation
    private Double latitude;
    private Double longitude;
    private String lieuSinistre;




}
