package cmr.notep.business.services;

import cmr.notep.business.business.UtilisateursBusiness;
import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.security.SimpleRateLimiter;
import cmr.notep.business.utils.JwtUtil;
import cmr.notep.interfaces.modeles.Utilisateurs;
import cmr.notep.modele.EtatUtilisateur;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Vérification du compte par un code envoyé par e-mail (bouton « Vérifier mon compte » de la page de connexion),
 * alternative au lien d'activation : pour un compte que son parcours a validé (ex. professeur validé par
 * l'administrateur : état PENDING avec jeton d'activation) mais qui n'a jamais cliqué sur le lien.
 *
 * <ul>
 *   <li>{@link #envoyer} : toujours silencieux (anti-énumération). Si le compte existe, un code à 6 chiffres
 *       valable {@value #VALIDITE_MINUTES} min est envoyé (haché en base, table verification_codes) ; pas de
 *       nouvel envoi avant {@value #DELAI_RENVOI_SECONDES} s.</li>
 *   <li>{@link #verifier} : {@value #MAX_ESSAIS} essais au plus par code. Après un code correct seulement,
 *       l'éligibilité du compte est vérifiée (sinon COMPTE_NON_ELIGIBLE) ; un compte éligible est activé comme
 *       par le lien (ActivationService) et le jeton d'activation est renvoyé pour choisir le mot de passe
 *       (POST /auth/registerPassword avec Authorization: Bearer &lt;activationToken&gt;).</li>
 * </ul>
 */
@Service
@Slf4j
public class VerificationCompteService {

    public static final int VALIDITE_MINUTES = 10;
    public static final int DELAI_RENVOI_SECONDES = 60;
    public static final int MAX_ESSAIS = 5;

    public static final String MESSAGE_ENVOI = "Si un compte existe pour cette adresse e-mail, un code de vérification "
            + "vient de lui être envoyé. Il est valable " + VALIDITE_MINUTES + " minutes.";

    private static final String TABLE = "ressources.verification_codes";

    private final SecureRandom random = new SecureRandom();
    private final JdbcTemplate jdbc;
    private final UtilisateursRepository utilisateursRepository;
    private final UtilisateursBusiness utilisateursBusiness;
    private final ActivationService activationService;
    private final RoleService roleService;
    private final JwtUtil jwtUtil;
    private final InscriptionClasseService inscriptionClasseService;
    private final VerificationCompteEmailService emailService;
    private final SimpleRateLimiter rateLimiter;

    public VerificationCompteService(JdbcTemplate jdbc, UtilisateursRepository utilisateursRepository,
                                     UtilisateursBusiness utilisateursBusiness, ActivationService activationService,
                                     RoleService roleService, JwtUtil jwtUtil,
                                     InscriptionClasseService inscriptionClasseService,
                                     VerificationCompteEmailService emailService, SimpleRateLimiter rateLimiter) {
        this.jdbc = jdbc;
        this.utilisateursRepository = utilisateursRepository;
        this.utilisateursBusiness = utilisateursBusiness;
        this.activationService = activationService;
        this.roleService = roleService;
        this.jwtUtil = jwtUtil;
        this.inscriptionClasseService = inscriptionClasseService;
        this.emailService = emailService;
        this.rateLimiter = rateLimiter;
    }

    // ─── Envoi ────────────────────────────────────────────────────────────────

    public void envoyer(String emailSaisi, String ip) {
        rateLimiter.verifier("verif-compte-envoi:" + ip, 10, Duration.ofMinutes(10),
                "Trop de demandes de code. Réessayez dans quelques minutes.");
        String email = normaliser(emailSaisi);
        if (email == null) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "L'adresse e-mail est obligatoire.");
        }
        var compteOpt = trouver(email);
        if (compteOpt.isEmpty()) {
            log.info("Account verification code requested for an unknown e-mail (ignored)");
            return;
        }
        var compte = compteOpt.get();
        String userId = compte.getId();
        LocalDateTime maintenant = LocalDateTime.now();
        List<Timestamp> derniers = jdbc.queryForList(
                "SELECT created_at FROM " + TABLE + " WHERE utilisateur_id = ? ORDER BY created_at DESC LIMIT 1",
                Timestamp.class, userId);
        if (!derniers.isEmpty() && derniers.get(0) != null
                && derniers.get(0).toLocalDateTime().isAfter(maintenant.minusSeconds(DELAI_RENVOI_SECONDES))) {
            log.info("Account verification code for {} not re-sent (cooldown {} s)", userId, DELAI_RENVOI_SECONDES);
            return;
        }
        String code = String.format("%06d", random.nextInt(1_000_000));
        jdbc.update("DELETE FROM " + TABLE + " WHERE utilisateur_id = ?", userId);
        jdbc.update("INSERT INTO " + TABLE + " (id, utilisateur_id, code_hash, expires_at, attempts, created_at) "
                        + "VALUES (?, ?, ?, ?, 0, ?)",
                UUID.randomUUID().toString(), userId, hacher(userId, code),
                Timestamp.valueOf(maintenant.plusMinutes(VALIDITE_MINUTES)), Timestamp.valueOf(maintenant));
        String nom = ((compte.getPrenom() == null ? "" : compte.getPrenom()) + " "
                + (compte.getNom() == null ? "" : compte.getNom())).trim();
        emailService.envoyerCode(compte.getEmail(), nom, code, VALIDITE_MINUTES);
        log.info("Account verification code sent to account {}", userId);
    }

    // ─── Vérification ─────────────────────────────────────────────────────────

    public Map<String, Object> verifier(String emailSaisi, String codeSaisi, String ip) {
        rateLimiter.verifier("verif-compte-code:" + ip, 30, Duration.ofMinutes(10),
                "Trop de tentatives de vérification. Réessayez dans quelques minutes.");
        String email = normaliser(emailSaisi);
        String code = codeSaisi == null ? "" : codeSaisi.replaceAll("\\s", "");
        if (email == null || !code.matches("\\d{6}")) {
            throw invalide(null);
        }
        var compteOpt = trouver(email);
        if (compteOpt.isEmpty()) {
            throw invalide(null);
        }
        var compte = compteOpt.get();
        String userId = compte.getId();
        List<Map<String, Object>> lignes = jdbc.queryForList(
                "SELECT id, code_hash, expires_at, attempts FROM " + TABLE
                        + " WHERE utilisateur_id = ? ORDER BY created_at DESC LIMIT 1", userId);
        if (lignes.isEmpty()) {
            throw invalide(null);
        }
        Map<String, Object> ligne = lignes.get(0);
        String id = (String) ligne.get("id");
        int essais = ((Number) ligne.get("attempts")).intValue();
        if (essais >= MAX_ESSAIS) {
            throw tropDEssais();
        }
        LocalDateTime expiration = ((Timestamp) ligne.get("expires_at")).toLocalDateTime();
        if (expiration.isBefore(LocalDateTime.now())) {
            throw new SchoolException(SchoolErrorCode.CODE_VERIFICATION_EXPIRE,
                    "Ce code de vérification a expiré. Demandez un nouveau code.");
        }
        if (!MessageDigest.isEqual(hacher(userId, code).getBytes(StandardCharsets.UTF_8),
                String.valueOf(ligne.get("code_hash")).getBytes(StandardCharsets.UTF_8))) {
            int restants = MAX_ESSAIS - (essais + 1);
            jdbc.update("UPDATE " + TABLE + " SET attempts = attempts + 1 WHERE id = ?", id);
            if (restants <= 0) {
                throw tropDEssais();
            }
            throw invalide(restants);
        }
        // Code correct : il ne peut plus resservir.
        jdbc.update("DELETE FROM " + TABLE + " WHERE utilisateur_id = ?", userId);
        log.info("Account verification code accepted for account {} (state {})", userId, compte.getEtat());
        return activerSiEligible(compte.getEmail());
    }

    /** Compte éligible : activé comme par le lien d'activation ; renvoie le jeton pour choisir le mot de passe. */
    private Map<String, Object> activerSiEligible(String email) {
        Utilisateurs u = utilisateursBusiness.avoirUtilisateurParEmail(email);
        EtatUtilisateur etat = u.getEtat();
        boolean sansMotDePasse = u.getPasseAccess() == null || u.getPasseAccess().isBlank();
        String jeton;
        if (etat == EtatUtilisateur.PENDING) {
            jeton = jetonActivationValide(u);
            activationService.activerUtilisateur(jeton); // même logique que POST /auth/activate
        } else if (etat == EtatUtilisateur.ACTIVE && sansMotDePasse && u.getActivationToken() != null) {
            // Lien d'activation déjà cliqué, mot de passe jamais choisi
            jeton = jetonActivationValide(u);
        } else {
            throw new SchoolException(SchoolErrorCode.COMPTE_NON_ELIGIBLE, messageNonEligible(u));
        }
        Map<String, Object> reponse = new LinkedHashMap<>();
        reponse.put("activationToken", jeton);
        reponse.put("email", u.getEmail());
        return reponse;
    }

    /** Jeton d'activation enregistré s'il est encore valide, sinon un nouveau (enregistré sur le compte). */
    private String jetonActivationValide(Utilisateurs u) {
        String actuel = u.getActivationToken();
        if (actuel != null && !actuel.isBlank() && jwtUtil.validateToken(actuel)) {
            return actuel;
        }
        String nouveau = jwtUtil.generateActivationToken(u.getEmail(), roleService.determineUserRoles(u));
        u.setActivationToken(nouveau);
        utilisateursBusiness.mettreUtilisateurAJour(u);
        return nouveau;
    }

    private String messageNonEligible(Utilisateurs u) {
        EtatUtilisateur etat = u.getEtat();
        if (etat == EtatUtilisateur.AWAITING_VALIDATION) {
            if (inscriptionClasseService.estEnAttenteInscriptionClasse(u.getId(), etat)) {
                return "Votre adresse e-mail est confirmée, mais votre compte est encore en attente d'approbation par "
                        + "le responsable de la classe. Vous recevrez vos identifiants par e-mail dès que votre "
                        + "demande sera acceptée.";
            }
            return "Votre adresse e-mail est confirmée, mais votre compte professeur est encore en attente de "
                    + "validation par l'administration (vérification de vos pièces justificatives). Vous recevrez "
                    + "un e-mail dès qu'il sera validé.";
        }
        if (etat == EtatUtilisateur.REJECTED) {
            return "Votre adresse e-mail est confirmée, mais votre demande de compte n'a pas été validée. "
                    + "Consultez l'e-mail reçu pour connaître le motif.";
        }
        if (etat == EtatUtilisateur.ACTIVE) {
            return "Votre compte est déjà activé : connectez-vous avec votre mot de passe, ou utilisez "
                    + "« Mot de passe oublié » si vous ne vous en souvenez plus.";
        }
        return "Votre adresse e-mail est confirmée, mais ce compte ne peut pas être activé pour le moment. "
                + "Contactez l'administration de ScholChat.";
    }

    // ─── Utilitaires ──────────────────────────────────────────────────────────

    private static SchoolException invalide(Integer restants) {
        return new SchoolException(SchoolErrorCode.CODE_VERIFICATION_INVALIDE,
                restants == null ? "Code de vérification invalide."
                        : "Code de vérification invalide. Il vous reste " + restants + " essai"
                        + (restants > 1 ? "s" : "") + ".");
    }

    private static SchoolException tropDEssais() {
        return new SchoolException(SchoolErrorCode.TROP_DE_TENTATIVES,
                "Trop d'essais avec ce code. Demandez un nouveau code de vérification.");
    }

    private static String normaliser(String email) {
        return email == null || email.isBlank() ? null : email.trim();
    }

    private java.util.Optional<cmr.notep.ressourcesjpa.dao.UtilisateursEntity> trouver(String email) {
        var compte = utilisateursRepository.findByEmail(email);
        return compte.isPresent() || email.equals(email.toLowerCase()) ? compte
                : utilisateursRepository.findByEmail(email.toLowerCase());
    }

    static String hacher(String userId, String code) {
        try {
            byte[] h = MessageDigest.getInstance("SHA-256")
                    .digest((userId + ":" + code).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(h);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
