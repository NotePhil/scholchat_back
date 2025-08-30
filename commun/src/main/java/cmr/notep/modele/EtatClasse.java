package cmr.notep.modele;

public enum EtatClasse {
    ACTIF,
    INACTIF,
    EN_ATTENTE_APPROBATION;

    public boolean isActive() {
        return this == ACTIF;
    }
}