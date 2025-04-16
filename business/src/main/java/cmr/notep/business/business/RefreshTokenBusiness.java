package cmr.notep.business.business;

import cmr.notep.business.config.JwtConfig;
import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.utils.JwtUtil;
import cmr.notep.interfaces.modeles.RefreshToken;
import cmr.notep.interfaces.modeles.Utilisateurs;
import cmr.notep.modele.EtatUtilisateur;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.RefreshTokenEntity;
import cmr.notep.ressourcesjpa.dao.UtilisateursEntity;
import cmr.notep.ressourcesjpa.repository.RefreshTokenRepository;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component

@Slf4j
public class RefreshTokenBusiness {

    private final DaoAccessorService daoAccessorService;
    private final JwtUtil jwtUtil;
    private final JwtConfig jwtConfig;

    public RefreshTokenBusiness(DaoAccessorService daoAccessorService, JwtConfig jwtConfig) {
        this.daoAccessorService = daoAccessorService;
        this.jwtUtil = new JwtUtil(jwtConfig);  // Now only passing one parameter
        this.jwtConfig = jwtConfig;
    }
    /**
     * Creates a new refresh token for a user
     *
     * @param utilisateur The user for whom to create a refresh token
     * @return The refresh token string
     */
    public String createRefreshToken(Utilisateurs utilisateur) {
        log.info("Creating refresh token for user: {}", utilisateur.getEmail());

        // Verify user status
        if (utilisateur.getEtat() != EtatUtilisateur.ACTIVE) {
            throw new SchoolException(
                    SchoolErrorCode.INACTIVE_USER,
                    "Cannot create refresh token for inactive user"
            );
        }

        // Delete any existing refresh tokens for this user to maintain single session
        deleteExistingRefreshTokens(utilisateur.getId());

        // Create new refresh token
        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();

        // Map the user model to entity
        UtilisateursEntity userEntity = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(utilisateur.getId())
                .orElseThrow(() -> new SchoolException(
                        SchoolErrorCode.NOT_FOUND,
                        "User not found with ID: " + utilisateur.getId()
                ));

        refreshTokenEntity.setUtilisateur(userEntity);
        refreshTokenEntity.setToken(generateUniqueToken());
        refreshTokenEntity.setExpiryDate(Instant.now().plusMillis(jwtConfig.getRefreshTokenExpirationMillis()));

        RefreshTokenEntity savedToken = daoAccessorService.getRepository(RefreshTokenRepository.class)
                .save(refreshTokenEntity);

        log.info("Refresh token created successfully for user: {}", utilisateur.getEmail());
        return savedToken.getToken();
    }

    /**
     * Finds a refresh token by its token string
     *
     * @param token The token string to find
     * @return Optional containing the refresh token if found
     */
    public Optional<RefreshToken> findByToken(String token) {
        log.debug("Looking up refresh token: {}", token);
        Optional<RefreshTokenEntity> refreshTokenEntity = daoAccessorService.getRepository(RefreshTokenRepository.class)
                .findByToken(token);

        return refreshTokenEntity.map(entity -> {
            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setId(entity.getId());
            refreshToken.setToken(entity.getToken());
            refreshToken.setExpiryDate(entity.getExpiryDate());
            refreshToken.setUtilisateur(dozerMapperBean.map(entity.getUtilisateur(), Utilisateurs.class));
            return refreshToken;
        });
    }

    /**
     * Deletes existing refresh tokens for a user
     *
     * @param userId The ID of the user whose tokens should be deleted
     */
    @Transactional
    public void deleteExistingRefreshTokens(String userId) {
        log.info("Deleting existing refresh tokens for user ID: {}", userId);
        daoAccessorService.getRepository(RefreshTokenRepository.class)
                .deleteByUtilisateurId(userId);
    }

    /**
     * Generates a unique token string
     *
     * @return A unique token string
     */
    private String generateUniqueToken() {
        return UUID.randomUUID().toString();
    }
}