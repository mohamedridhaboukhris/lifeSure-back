package com.assurance.Entity;

public enum StatutContrat {
    EN_ATTENTE_VALIDATION,  // 🆕 Soumis par client, attend l'agent

    EN_ATTENTE,
    ACTIF,
    SUSPENDU,
    RESILIE,
    EXPIRE,
    ANNULE
}
