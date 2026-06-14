package com.assurance.Entity;

public enum TypeReclamation {
    CONTESTATION_SINISTRE,      // Contester un sinistre refusé
    CONTESTATION_MONTANT,       // Contester le montant d'indemnisation
    CONTESTATION_CONTRAT,       // Contester une décision sur contrat
    DELAI_TRAITEMENT,           // Délai de traitement trop long
    QUALITE_SERVICE,            // Qualité du service
    AUTRE
}
