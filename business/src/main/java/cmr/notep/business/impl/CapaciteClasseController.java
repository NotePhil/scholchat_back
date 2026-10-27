package cmr.notep.business.impl;

import cmr.notep.business.security.AccessControlService;
import cmr.notep.business.services.CapaciteClasseService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * GET /classes/{classeId}/capacite — restriction « Élèves max » de l'offre de la classe :
 * { classeId, elevesMax (null = illimité), nombreEleves, complete }. Membres de la classe et gestionnaires.
 */
@RestController
public class CapaciteClasseController {

    private final CapaciteClasseService capaciteClasseService;
    private final AccessControlService accessControl;

    public CapaciteClasseController(CapaciteClasseService capaciteClasseService, AccessControlService accessControl) {
        this.capaciteClasseService = capaciteClasseService;
        this.accessControl = accessControl;
    }

    @GetMapping(path = "/classes/{classeId}/capacite", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> capacite(@PathVariable String classeId) {
        accessControl.requireClassMember(classeId);
        return capaciteClasseService.capacite(classeId);
    }
}
