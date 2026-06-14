package com.assurance.Entity;

public enum TypeSinistre {
    // AUTO
    ACCIDENT(TypeContrat.AUTO),
    VOL(TypeContrat.AUTO),
    INCENDIE_AUTO(TypeContrat.AUTO),
    BRIS_DE_GLACE(TypeContrat.AUTO),
    DOMMAGES_TOUS_ACCIDENTS(TypeContrat.AUTO),

    // HABITATION
    INCENDIE_HABITATION(TypeContrat.HABITATION),
    DEGAT_EAUX(TypeContrat.HABITATION),
    VOL_HABITATION(TypeContrat.HABITATION),
    CATASTROPHE_NATURELLE(TypeContrat.HABITATION),
    RESPONSABILITE_CIVILE(TypeContrat.HABITATION),

    // SANTE
    HOSPITALISATION(TypeContrat.SANTE),
    CONSULTATION(TypeContrat.SANTE),
    SOINS_DENTAIRES(TypeContrat.SANTE),
    MALADIE_GRAVE(TypeContrat.SANTE),
    PHARMACIE(TypeContrat.SANTE),

    // VOYAGE
    ANNULATION(TypeContrat.VOYAGE),
    PERTE_BAGAGES(TypeContrat.VOYAGE),
    ACCIDENT_VOYAGE(TypeContrat.VOYAGE),
    SOINS_MEDICAUX(TypeContrat.VOYAGE),
    RETARD_VOL(TypeContrat.VOYAGE);
    private final TypeContrat contrat;
    TypeSinistre(TypeContrat contrat) {
        this.contrat = contrat;
    }
    public TypeContrat getContrat() {
        return contrat;
    }
}
