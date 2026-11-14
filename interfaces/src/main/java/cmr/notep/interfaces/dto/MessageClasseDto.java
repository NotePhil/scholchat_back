package cmr.notep.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Classe à laquelle l'utilisateur peut envoyer un message de groupe. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageClasseDto {
    private String id;
    private String nom;
    private String niveau;
    /** Parent : ses enfants inscrits dans cette classe (pour ne proposer que les classes de l'enfant sélectionné). */
    private java.util.List<String> eleveIds = new java.util.ArrayList<>();

    public MessageClasseDto(String id, String nom, String niveau) {
        this.id = id;
        this.nom = nom;
        this.niveau = niveau;
    }
}
