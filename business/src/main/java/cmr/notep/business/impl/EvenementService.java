
package cmr.notep.business.impl;

import cmr.notep.business.business.EvenementBusiness;
import cmr.notep.business.business.InteractionBusiness;
import cmr.notep.interfaces.api.EvenementApi;
import cmr.notep.interfaces.modeles.Evenement;
import cmr.notep.interfaces.modeles.Interaction;
import cmr.notep.interfaces.modeles.CommentRequest;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.AccederEntity;
import cmr.notep.ressourcesjpa.dao.UtilisateursEntity;
import cmr.notep.ressourcesjpa.repository.AccederRepository;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class EvenementService implements EvenementApi {

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
        log.info("Création d'un nouvel événement: {}", evenement.getTitre());
        return evenementBusiness.creerEvenement(evenement);
    }
    
    @Override
    public List<Evenement> obtenirTousEvenements() {
        log.info("Récupération de tous les événements");
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
        
        return filteredEvents;
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
        log.info("Mise à jour de l'événement avec ID: {}", id);
        return evenementBusiness.mettreAJourEvenement(id, evenement);
    }

    @Override
    public void supprimerEvenement(@NonNull String id) {
        log.info("Suppression de l'événement avec ID: {}", id);
        evenementBusiness.supprimerEvenement(id);
    }

    @Override
    public Evenement obtenirEvenementParId(@NonNull String id) {
        log.info("Récupération de l'événement avec ID: {}", id);
        return evenementBusiness.obtenirEvenementParId(id);
    }

    @Override
    public List<Evenement> obtenirEvenementsParProfesseur(@NonNull String professeurId) {
        log.info("Récupération des événements pour le professeur avec ID: {}", professeurId);
        return evenementBusiness.obtenirEvenementsParProfesseur(professeurId);
    }

    // Nouveaux endpoints pour les interactions
    @Override
    public void likerEvenement(@NonNull String eventId) {
        String userId = getCurrentUserId();
        log.info("Utilisateur {} toggle like pour l'événement {}", userId, eventId);
        
        // Use toggle functionality instead of always creating new like
        interactionBusiness.toggleLike(eventId, userId);
    }

    @Override
    public Interaction commenterEvenement(@NonNull String eventId, @NonNull CommentRequest commentRequest) {
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
    public void rejoindreEvenement(@NonNull String eventId) {
        String userId = getCurrentUserId();
        log.info("Utilisateur {} rejoint l'événement {}", userId, eventId);
        interactionBusiness.joinEvent(eventId, userId);
    }

    @Override
    public void quitterEvenement(@NonNull String eventId) {
        String userId = getCurrentUserId();
        log.info("Utilisateur {} quitte l'événement {}", userId, eventId);
        interactionBusiness.unjoinEvent(eventId, userId);
    }
}
