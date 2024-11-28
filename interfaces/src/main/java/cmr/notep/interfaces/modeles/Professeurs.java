package cmr.notep.interfaces.modeles;

import lombok.*;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Professeurs {
    private Long id; // ID auto-incrémenté

    @NotNull
    private String urlCni; // Champ obligatoire pour identification unique

    private String urlPhoto;

    @NotNull
    private String nomEtablissement;

    @NotNull
    private String nomClasse;

    @NotNull
    private String matriculeProfesseur;
}
