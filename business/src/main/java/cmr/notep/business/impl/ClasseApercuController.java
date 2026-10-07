package cmr.notep.business.impl;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.security.SimpleRateLimiter;
import cmr.notep.business.services.InscriptionClasseService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

/**
 * GET /public/classes/apercu?code=XXXX&type=parent|eleve — route publique : affiche la classe correspondant à un
 * code avant l'inscription (ou l'ajout d'un profil). Mêmes validations et mêmes erreurs que l'inscription
 * (InscriptionClasseService#resoudreClasse) ; limitée à {@value #MAX_REQUETES} requêtes par IP et par
 * {@value #FENETRE_MINUTES} minutes (429 TROP_DE_TENTATIVES) contre l'énumération des codes.
 */
@RestController
@RequestMapping("/public/classes")
public class ClasseApercuController {

    static final int MAX_REQUETES = 20;
    static final int FENETRE_MINUTES = 10;

    private final InscriptionClasseService inscriptionClasseService;
    private final SimpleRateLimiter rateLimiter;

    public ClasseApercuController(InscriptionClasseService inscriptionClasseService, SimpleRateLimiter rateLimiter) {
        this.inscriptionClasseService = inscriptionClasseService;
        this.rateLimiter = rateLimiter;
    }

    @GetMapping(path = "/apercu", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> apercu(@RequestParam(name = "code", required = false) String code,
                                      @RequestParam(name = "type", required = false) String type,
                                      HttpServletRequest request) {
        rateLimiter.verifier("apercu-classe:" + SimpleRateLimiter.ipClient(request), MAX_REQUETES,
                Duration.ofMinutes(FENETRE_MINUTES),
                "Trop de recherches de code de classe. Réessayez dans quelques minutes.");
        String t = type == null ? "" : type.trim().toLowerCase();
        boolean pourEleve;
        switch (t) {
            case "eleve", "élève", "student" -> pourEleve = true;
            case "parent" -> pourEleve = false;
            default -> throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "Paramètre type invalide : valeurs acceptées « parent » ou « eleve ».");
        }
        return inscriptionClasseService.apercu(code, pourEleve);
    }
}
