package cmr.notep.business.services;

import cmr.notep.ressourcesjpa.dao.MotifRejetEntity;
import cmr.notep.ressourcesjpa.dao.ProfesseursEntity;
import org.springframework.scheduling.annotation.Async;

public interface IRejectionEmailService {
    @Async
    void sendRejectionEmail(ProfesseursEntity professeur, MotifRejetEntity motif, String motifSupplementaire);
}
