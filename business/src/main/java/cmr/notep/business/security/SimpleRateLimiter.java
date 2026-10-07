package cmr.notep.business.security;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limitation de débit simple, en mémoire (fenêtre glissante par clé) : protège les routes publiques
 * sensibles à l'énumération (aperçu d'une classe par son code, codes de vérification du compte…).
 * Mémoire locale à l'instance : suffisant pour freiner une énumération, pas une protection distribuée.
 */
@Component
public class SimpleRateLimiter {

    private static final int NETTOYAGE_SEUIL = 10_000;

    private final Map<String, Deque<Long>> fenetres = new ConcurrentHashMap<>();

    /**
     * Enregistre une tentative pour {@code cle} ; lève TROP_DE_TENTATIVES (HTTP 429) si plus de {@code max}
     * tentatives ont eu lieu dans la fenêtre {@code fenetre}.
     */
    public void verifier(String cle, int max, Duration fenetre, String message) {
        if (!tenter(cle, max, fenetre)) {
            throw new SchoolException(SchoolErrorCode.TROP_DE_TENTATIVES, message);
        }
    }

    /** @return vrai si la tentative est acceptée (et comptée), faux si la limite est atteinte. */
    public boolean tenter(String cle, int max, Duration fenetre) {
        long maintenant = System.currentTimeMillis();
        long debut = maintenant - fenetre.toMillis();
        if (fenetres.size() > NETTOYAGE_SEUIL) {
            nettoyer(debut);
        }
        Deque<Long> horodatages = fenetres.computeIfAbsent(cle, k -> new ArrayDeque<>());
        synchronized (horodatages) {
            while (!horodatages.isEmpty() && horodatages.peekFirst() < debut) {
                horodatages.pollFirst();
            }
            if (horodatages.size() >= max) {
                return false;
            }
            horodatages.addLast(maintenant);
            return true;
        }
    }

    private void nettoyer(long debut) {
        fenetres.entrySet().removeIf(e -> {
            synchronized (e.getValue()) {
                return e.getValue().isEmpty() || e.getValue().peekLast() < debut;
            }
        });
    }

    /**
     * Adresse IP du client : DERNIER élément de X-Forwarded-For (celui ajouté par le proxy de
     * l'hébergeur, que le client ne peut pas falsifier ; les premiers éléments viennent du client),
     * sinon l'adresse distante.
     */
    public static String ipClient(HttpServletRequest request) {
        if (request == null) {
            return "inconnue";
        }
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            String[] parts = xff.split(",");
            String last = parts[parts.length - 1].trim();
            if (!last.isEmpty()) {
                return last;
            }
        }
        return request.getRemoteAddr();
    }
}
