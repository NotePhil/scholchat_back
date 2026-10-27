
package cmr.notep.business.impl;

import cmr.notep.business.business.EvenementBusiness;
import cmr.notep.business.business.EvenementStatut;
import cmr.notep.business.business.InteractionBusiness;
import cmr.notep.interfaces.api.EvenementApi;
import cmr.notep.interfaces.modeles.Evenement;
import cmr.notep.interfaces.modeles.Interaction;
import cmr.notep.interfaces.modeles.CommentRequest;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.interfaces.modeles.PagedResponse;
import cmr.notep.ressourcesjpa.dao.AccederEntity;
import cmr.notep.ressourcesjpa.dao.UtilisateursEntity;
import cmr.notep.ressourcesjpa.repository.AccederRepository;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@Slf4j
public class EvenementService implements EvenementApi {
    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.security.CurrentUserService currentUser;
    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.security.AccessControlService accessControl;
    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.security.ParentSansEnfantValide parentSansEnfantValide;

    /**
     * Session parent dont aucun enfant n'est encore accepté dans une classe : le fil ne contient que les
     * actualités générales (événements publics sans classe ciblée), quel que soit {@code eleveId}.
     */
    private boolean parentLimite() {
        org.springframework.web.context.request.RequestAttributes attrs =
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        return attrs instanceof org.springframework.web.context.request.ServletRequestAttributes sra
                && parentSansEnfantValide.estParentLimite(sra.getRequest());
    }

    private static boolean estPublicGeneral(Evenement e) {
        return !"PRIVATE".equals(e.getVisibility()) && (e.getClassesIds() == null || e.getClassesIds().isEmpty());
    }

    /** Visibilité d'un événement : PUBLIC pour tous ; PRIVATE pour l'auteur, les admins et les membres des classes ciblées. */
    private boolean peutVoir(Evenement e) {
        if (e == null) return false;
        if (!"PRIVATE".equals(e.getVisibility())) return true;
        if (currentUser.isAdmin()) return true;
        String me = currentUser.requireUserId();
        if (me.equals(e.getCreateurId())) return true;
        List<String> classes = e.getClassesIds();
        if (classes == null || classes.isEmpty()) return true; // comportement existant : privé sans classe = visible
        return classes.stream().anyMatch(c -> accessControl.isClassMember(c, me));
    }

    private void exigerVisible(String eventId) {
        if (!peutVoir(evenementBusiness.obtenirEvenementParId(eventId))) {
            throw cmr.notep.business.security.CurrentUserService.forbidden("Cet événement est réservé aux membres des classes concernées.");
        }
    }

    private void exigerAuteur(String eventId) {
        Evenement e = evenementBusiness.obtenirEvenementParId(eventId);
        if (!currentUser.isAdmin() && !currentUser.isSelf(e.getCreateurId())) {
            throw cmr.notep.business.security.CurrentUserService.forbidden("Seul l'auteur de l'événement peut le modifier.");
        }
    }


    private final EvenementBusiness evenementBusiness;
    private final InteractionBusiness interactionBusiness;
    private final DaoAccessorService daoAccessorService;

    public EvenementService(EvenementBusiness evenementBusiness, InteractionBusiness interactionBusiness, DaoAccessorService daoAccessorService) {
        this.evenementBusiness = evenementBusiness;
        this.interactionBusiness = interactionBusiness;
        this.daoAccessorService = daoAccessorService;
    }

    // Resolve the authenticated user's email to their actual user ID
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            UtilisateursEntity user = daoAccessorService.getRepository(UtilisateursRepository.class)
                    .findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
            return user.getId();
        }
        throw new RuntimeException("Utilisateur non authentifié");
    }

    @Override
    public Evenement creerEvenement(@NonNull Evenement evenement) {
        currentUser.requireAuthenticated();
        if (!currentUser.isAdmin()) {
            evenement.setCreateurId(currentUser.requireUserId());
            if (evenement.getClassesIds() != null) evenement.getClassesIds().forEach(accessControl::requireClassTeacher);
        }
        log.info("Création d'un nouvel événement: {}", evenement.getTitre());
        return EvenementStatut.appliquer(evenementBusiness.creerEvenement(evenement));
    }
    
    /**
     * Classes de l'élève dont on affiche le fil : l'élève lui-même ou l'un de ses parents (enfant
     * sélectionné, mineur ou majeur), ou un administrateur. 403 sinon.
     */
    private List<String> classesDuFilEleve(String eleveId) {
        String me = getCurrentUserId();
        if (!me.equals(eleveId) && !currentUser.isAdmin() && !accessControl.isParentOf(me, eleveId)) {
            throw cmr.notep.business.security.CurrentUserService.forbidden(
                    "Vous ne pouvez consulter que votre fil ou celui de vos enfants.");
        }
        java.util.Set<String> lisibles = accessControl.classesLisiblesParParent(eleveId);
        List<String> classes = getUserClasses(eleveId);
        // Parent d'un enfant majeur : fil des classes dont le suivi est accepté
        return lisibles == null ? classes : classes.stream().filter(lisibles::contains).toList();
    }

    @Override
    public PagedResponse<Evenement> obtenirEvenementsPagines(int page, int size, String eleveId) {
        log.info("Récupération paginée des événements (page={}, size={}, eleveId={})", page, size, eleveId);
        PagedResponse<Evenement> res;
        if (parentLimite()) {
            res = evenementBusiness.obtenirPublicsGeneraux(page, size);
        } else if (eleveId != null && !eleveId.isBlank()) {
            res = evenementBusiness.obtenirFilPourClasses(page, size, classesDuFilEleve(eleveId));
        } else {
            String userId = getCurrentUserId();
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            List<String> userClasses = isAdmin ? java.util.Collections.emptyList() : getUserClasses(userId);
            res = evenementBusiness.obtenirEvenementsPagines(page, size, userClasses, isAdmin);
        }
        EvenementStatut.appliquer(res.getContent());
        return res;
    }

    @Override
    public List<Evenement> obtenirTousEvenements(String eleveId) {
        log.info("Récupération de tous les événements (eleveId={})", eleveId);
        if (parentLimite()) {
            List<Evenement> publics = evenementBusiness.obtenirTousEvenements().stream()
                    .filter(EvenementService::estPublicGeneral)
                    .collect(java.util.stream.Collectors.toList());
            publics.forEach(ev -> ev.setInteractions(interactionBusiness.getInteractionsByEventId(ev.getId())));
            return EvenementStatut.appliquer(publics);
        }
        if (eleveId != null && !eleveId.isBlank()) {
            // Même règle que le fil paginé : publics + ceux qui ciblent les classes de l'élève.
            List<String> classes = classesDuFilEleve(eleveId);
            return EvenementStatut.appliquer(evenementBusiness.obtenirTousEvenements().stream()
                    .filter(ev -> !"PRIVATE".equals(ev.getVisibility())
                            || (ev.getClassesIds() != null && ev.getClassesIds().stream().anyMatch(classes::contains)))
                    .collect(java.util.stream.Collectors.toList()));
        }
        String userId = getCurrentUserId();
        List<Evenement> evenements = evenementBusiness.obtenirTousEvenements();
        
        // Filter events based on visibility and user's classes
        List<Evenement> filteredEvents = evenements.stream()
                .filter(evenement -> {
                    // Always show public events
                    if ("PUBLIC".equals(evenement.getVisibility())) {
                        return true;
                    }
                    
                    // For private events, check if user is in selected classes
                    if ("PRIVATE".equals(evenement.getVisibility())) {
                        // Admin can see all events
                        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                        if (auth != null && auth.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                            return true;
                        }
                        
                        List<String> userClasses = getUserClasses(userId);
                        List<String> eventClasses = evenement.getClassesIds();
                        
                        // If no classes selected for private event, show to everyone
                        if (eventClasses == null || eventClasses.isEmpty()) {
                            return true;
                        }
                        
                        // Check if user has any class in common with event
                        return userClasses.stream().anyMatch(eventClasses::contains);
                    }
                    
                    return true; // Default: show event
                })
                .collect(java.util.stream.Collectors.toList());
        
        // Add interactions to each filtered event
        for (Evenement evenement : filteredEvents) {
            List<Interaction> interactions = interactionBusiness.getInteractionsByEventId(evenement.getId());
            evenement.setInteractions(interactions);
        }
        
        return EvenementStatut.appliquer(filteredEvents);
    }
    
    // Helper method to get user's classes
    private List<String> getUserClasses(String userId) {
        try {
            // Get user's classes from AccederEntity (user-class relationship)
            List<AccederEntity> userAccess = daoAccessorService.getRepository(AccederRepository.class)
                    .findByUtilisateurId(userId);
            
            return userAccess.stream()
                    .map(AccederEntity::getClasseId)
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting user classes for user {}: {}", userId, e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
    
    @Override
    public Evenement mettreAJourEvenement(@NonNull String id, @NonNull Evenement evenement) {
        exigerAuteur(id);
        if (!currentUser.isAdmin()) {
            evenement.setCreateurId(null);
            if (evenement.getClassesIds() != null) evenement.getClassesIds().forEach(accessControl::requireClassTeacher);
        }
        log.info("Mise à jour de l'événement avec ID: {}", id);
        return EvenementStatut.appliquer(evenementBusiness.mettreAJourEvenement(id, evenement));
    }

    @Override
    public void supprimerEvenement(@NonNull String id) {
        exigerAuteur(id);
        log.info("Suppression de l'événement avec ID: {}", id);
        evenementBusiness.supprimerEvenement(id);
    }

    @Override
    public Evenement obtenirEvenementParId(@NonNull String id) {
        exigerVisible(id);
        log.info("Récupération de l'événement avec ID: {}", id);
        return EvenementStatut.appliquer(evenementBusiness.obtenirEvenementParId(id));
    }

    @Override
    public List<Evenement> obtenirEvenementsParProfesseur(@NonNull String professeurId) {
        log.info("Récupération des événements pour le professeur avec ID: {}", professeurId);
        return EvenementStatut.appliquer(evenementBusiness.obtenirEvenementsParProfesseur(professeurId).stream()
                .filter(this::peutVoir)
                .collect(java.util.stream.Collectors.toList()));
    }

    // Nouveaux endpoints pour les interactions
    @Override
    public void likerEvenement(@NonNull String eventId) {
        exigerVisible(eventId);
        String userId = getCurrentUserId();
        log.info("Utilisateur {} toggle like pour l'événement {}", userId, eventId);
        
        // Use toggle functionality instead of always creating new like
        interactionBusiness.toggleLike(eventId, userId);
    }

    @Override
    public Interaction commenterEvenement(@NonNull String eventId, @NonNull CommentRequest commentRequest) {
        exigerVisible(eventId);
        String userId = getCurrentUserId();
        String commentContent = commentRequest.getContent();
        log.info("Utilisateur {} commente l'événement {}", userId, eventId);
        
        Interaction commentInteraction = Interaction.builder()
                .type(cmr.notep.modele.InteractionType.COMMENT)
                .content(commentContent)
                .niveau("INFO")
                .createdById(userId)
                .eventId(eventId)
                .build();
        
        return interactionBusiness.createInteraction(commentInteraction);
    }

    @Override
    public void supprimerCommentaire(@NonNull String eventId, @NonNull String commentId) {
        String me = currentUser.requireUserId();
        Evenement e = evenementBusiness.obtenirEvenementParId(eventId);
        Interaction commentaire = interactionBusiness.getCommentaireEvenement(eventId, commentId);
        boolean autorise = currentUser.isAdmin()
                || me.equals(commentaire.getCreatedById())
                || me.equals(e.getCreateurId())
                || (e.getClassesIds() != null && e.getClassesIds().stream().anyMatch(c -> accessControl.isClassManager(c, me)));
        if (!autorise) {
            throw cmr.notep.business.security.CurrentUserService.forbidden(
                    "Seuls l'auteur du commentaire, l'auteur de la publication et les modérateurs peuvent supprimer ce commentaire.");
        }
        log.info("Utilisateur {} supprime le commentaire {} de l'événement {}", me, commentId, eventId);
        interactionBusiness.supprimerInteraction(commentId);
    }

    @Override
    public void rejoindreEvenement(@NonNull String eventId) {
        exigerVisible(eventId);
        String userId = getCurrentUserId();
        log.info("Utilisateur {} rejoint l'événement {}", userId, eventId);
        interactionBusiness.joinEvent(eventId, userId);
    }

    @Override
    public void quitterEvenement(@NonNull String eventId) {
        currentUser.requireAuthenticated();
        String userId = getCurrentUserId();
        log.info("Utilisateur {} quitte l'événement {}", userId, eventId);
        interactionBusiness.unjoinEvent(eventId, userId);
    }
}
