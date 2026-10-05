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
}
