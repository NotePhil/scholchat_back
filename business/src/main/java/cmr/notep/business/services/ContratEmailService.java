package cmr.notep.business.services;

import cmr.notep.ressourcesjpa.dao.ContratEntity;
import org.springframework.scheduling.annotation.Async;

@Async
public interface ContratEmailService {
    void sendConfirmationSouscriptionEmail(ContratEntity contrat);

    void sendOffreExpirationBientotEmail(ContratEntity contrat, String emailDestinataire);

    void sendOffreExpireeEmail(ContratEntity contrat, String emailDestinataire);

    void sendRenouvellementLienEmail(String email, String nomCible, String entityType, String entityId, String token);

    void sendSuppressionImminenteEmail(ContratEntity contrat, String emailDestinataire, java.time.LocalDateTime dateSuppressionPrevue);

    void sendEntiteSupprimeeEmail(String email, String nomCible, String offreNom);
}
