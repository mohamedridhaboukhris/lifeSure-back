package com.assurance.Service;

import com.assurance.Entity.TauxAssurance;
import com.assurance.Entity.TypeContrat;

import java.math.BigDecimal;
import java.util.List;

public interface TauxAssuranceService {

    TauxAssurance setTaux(TypeContrat type, BigDecimal taux);
    List<TauxAssurance> getAllTaux();
    // Récupérer le taux actif pour un type de contrat

    TauxAssurance ajouterTaux(TypeContrat type, BigDecimal taux);
}
