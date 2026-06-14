package com.assurance.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PlafondCheckResponse {

    private BigDecimal plafond;
    private BigDecimal montantDejaPaye;
    private BigDecimal montantRestant;
    private String message;
    private boolean depasse;
}