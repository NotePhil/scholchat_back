package cmr.notep.business.services;

import cmr.notep.ressourcesjpa.dao.MotifRejetEntity;
import cmr.notep.ressourcesjpa.dao.ProfesseursEntity;
import org.springframework.scheduling.annotation.Async;

public interface IRejectionEmailService {
    @Async
    void sendRejectionEmail(ProfesseursEntity professeur, MotifRejetEntity motif, String motifSupplementaire);

    /**
     * @param compteActif vrai pour un compte déjà actif (demande de profil professeur d'un parent / élève, ou
     *                    professeur activé partiellement) : l'e-mail invite à redéposer les pièces depuis le profil.
     */
    @Async
    default void sendRejectionEmail(ProfesseursEntity professeur, MotifRejetEntity motif, String motifSupplementaire,
                                    boolean compteActif) {
        sendRejectionEmail(professeur, motif, motifSupplementaire);
    }
}
