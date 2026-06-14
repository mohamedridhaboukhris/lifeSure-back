package com.assurance.Service;

import com.assurance.Entity.Paiement;

import java.util.List;

public interface PaiementService {
    Paiement effectuerPaiement(Long contratId, Paiement paiement);

    List<Paiement> getPaiementsByContrat(Long contratId);
    List<Paiement> getPaiementsByClient(Long clientId);
}




