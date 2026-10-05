package cmr.notep.business.security;

import cmr.notep.ressourcesjpa.dao.ElevesEntity;
import cmr.notep.ressourcesjpa.dao.GestionnairesEntity;
import cmr.notep.ressourcesjpa.dao.ParentsEntity;
import cmr.notep.ressourcesjpa.dao.ProfesseursEntity;
import cmr.notep.ressourcesjpa.dao.RepetiteursEntity;
import cmr.notep.ressourcesjpa.dao.UtilisateursEntity;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Rôles « structurels » d'un utilisateur, lus dans les tables filles (professeurs, parents, eleves…).
 *
 * Un compte multi-rôles (ex. parent ET professeur) a une ligne dans plusieurs tables filles, mais
 * Hibernate (héritage JOINED) ne matérialise qu'UN sous-type par id et par contexte de persistance,
 * choisi arbitrairement. Il ne faut donc jamais décider d'un rôle via {@code instanceof
 * ProfesseursEntity/ParentsEntity…} : utiliser ces méthodes (requêtes natives d'existence), et pour
 * les associations, l'entité de base {@link UtilisateursEntity}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserSubtypeService {

    private final UtilisateursRepository utilisateursRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public boolean isProfesseur(String userId) {
        return userId != null && utilisateursRepository.hasProfesseurRow(userId);
    }

    public boolean isParent(String userId) {
        return userId != null && utilisateursRepository.hasParentRow(userId);
    }

    public boolean isEleve(String userId) {
        return userId != null && utilisateursRepository.hasEleveRow(userId);
    }

    public boolean isRepetiteur(String userId) {
        return userId != null && utilisateursRepository.hasRepetiteurRow(userId);
    }

    public boolean isGestionnaire(String userId) {
        return userId != null && utilisateursRepository.hasGestionnaireRow(userId);
    }

    public boolean isProfesseur(UtilisateursEntity u) {
        return u != null && isProfesseur(u.getId());
    }

    public boolean isParent(UtilisateursEntity u) {
        return u != null && isParent(u.getId());
    }

    public boolean isEleve(UtilisateursEntity u) {
        return u != null && isEleve(u.getId());
    }

    /**
     * L'utilisateur (entité de base, quel que soit le sous-type chargé) s'il a le rôle professeur.
     * À utiliser à la place de {@code ProfesseursRepository.findById}, qui renvoie vide quand le
     * compte est déjà chargé sous un autre sous-type dans le contexte de persistance.
     */
    public Optional<UtilisateursEntity> findProfesseur(String userId) {
        if (!isProfesseur(userId)) {
            return Optional.empty();
        }
        return utilisateursRepository.findById(userId);
    }

    /**
     * Type d'affichage, déterministe (même priorité que les anciens tests instanceof) :
     * PROFESSEUR, ELEVE, REPETITEUR, PARENT, GESTIONNAIRE, sinon null.
     */
    public String typeUtilisateur(String userId) {
        if (userId == null) return null;
        // Appelé en boucle (listes de messages, membres de classe) : une requête par id, mise en
        // cache pour la durée de la requête HTTP.
        Map<String, Optional<String>> cache = requestCache();
        if (cache != null && cache.containsKey(userId)) {
            return cache.get(userId).orElse(null);
        }
        String type = null;
        List<Object[]> rows = utilisateursRepository.findSubtypeFlags(userId);
        if (!rows.isEmpty()) {
            Object[] f = rows.get(0);
            String[] types = {"PROFESSEUR", "ELEVE", "REPETITEUR", "PARENT", "GESTIONNAIRE"};
            for (int i = 0; i < types.length && i < f.length; i++) {
                if (Boolean.TRUE.equals(f[i])) {
                    type = types[i];
                    break;
                }
            }
        }
        if (cache != null) {
            cache.put(userId, Optional.ofNullable(type));
        }
        return type;
    }

    private static final String TYPE_CACHE_ATTR = UserSubtypeService.class.getName() + ".types";

    @SuppressWarnings("unchecked")
    private Map<String, Optional<String>> requestCache() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        Object cached = attrs.getAttribute(TYPE_CACHE_ATTR, RequestAttributes.SCOPE_REQUEST);
        if (cached instanceof Map<?, ?> m) {
            return (Map<String, Optional<String>>) m;
        }
        Map<String, Optional<String>> m = new HashMap<>();
        attrs.setAttribute(TYPE_CACHE_ATTR, m, RequestAttributes.SCOPE_REQUEST);
        return m;
    }

    /**
     * Charge l'utilisateur sous le sous-type demandé, pour les seuls cas où les colonnes propres
     * à ce sous-type sont nécessaires (ex. pièces du professeur).
     *
     * Si le compte est déjà managé sous un AUTRE sous-type dans le contexte de persistance, un
     * {@code findById} du sous-type renverrait vide : l'instance déjà chargée est alors synchronisée
     * (flush si une transaction est active) puis détachée, et le sous-type demandé est rechargé.
     * Préférer l'entité de base / les ids partout ailleurs.
     */
    public <T extends UtilisateursEntity> Optional<T> findSubtype(Class<T> type, String userId) {
        if (userId == null || !hasSubtypeRow(type, userId)) {
            return Optional.empty();
        }
        T found = entityManager.find(type, userId);
        if (found != null) {
            return Optional.of(found);
        }
        // La ligne existe : le compte est donc déjà managé sous un autre sous-type.
        UtilisateursEntity loaded = entityManager.find(UtilisateursEntity.class, userId);
        if (loaded == null) {
            return Optional.empty();
        }
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            entityManager.flush();
        }
        log.debug("Compte multi-rôles {} : {} détaché pour charger {}", userId,
                Hibernate.getClass(loaded).getSimpleName(), type.getSimpleName());
        entityManager.detach(loaded);
        return Optional.ofNullable(entityManager.find(type, userId));
    }

    private boolean hasSubtypeRow(Class<? extends UtilisateursEntity> type, String userId) {
        if (type == ProfesseursEntity.class) return isProfesseur(userId);
        if (type == ParentsEntity.class) return isParent(userId);
        if (type == ElevesEntity.class) return isEleve(userId);
        if (type == RepetiteursEntity.class) return isRepetiteur(userId);
        if (type == GestionnairesEntity.class) return isGestionnaire(userId);
        return utilisateursRepository.existsById(userId);
    }
}
