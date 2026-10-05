package cmr.notep.business.business;

import cmr.notep.business.security.UserSubtypeService;

import cmr.notep.business.config.time.ServerDateTimes;
import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.services.MediaService;
import cmr.notep.business.services.MessagePublisher;
import cmr.notep.business.services.NotificationService;
import cmr.notep.interfaces.dto.GroupMessageDto;
import cmr.notep.interfaces.dto.MessageBulkDeleteRequest;
import cmr.notep.interfaces.dto.MessageClasseDto;
import cmr.notep.interfaces.dto.MessageStatutDTO;
import cmr.notep.interfaces.modeles.*;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.*;
import cmr.notep.ressourcesjpa.repository.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

/**
 * Messagerie.
 *
 * Règles :
 * - l'expéditeur est toujours l'utilisateur authentifié (JWT), jamais le corps de la requête ;
 * - on ne peut écrire qu'aux personnes partageant une classe avec soi (membre, modérateur,
 *   créateur, droit de publication — directement ou via ses enfants pour un parent), à sa
 *   famille (parent/enfant) et aux administrateurs ; un administrateur peut écrire à tous ;
 * - "supprimer pour moi" (MessageStatut.supprime) masque le message pour l'appelant seulement ;
 *   "supprimer pour tout le monde" (Messages.deleted) est réservé à l'expéditeur ;
 * - la corbeille, sa restauration et son vidage sont propres à chaque utilisateur.
 */
@Component
@Slf4j
@Transactional
public class MessagesBusiness {

    public static final String SCOPE_ME = "me";
    public static final String SCOPE_EVERYONE = "everyone";
    private static final int MAX_MEDIAS = 10;
    private static final String MSG_INTERDIT_DESTINATAIRE =
            "Vous ne pouvez écrire qu'aux membres de vos classes (enseignants, élèves et leurs parents) et aux administrateurs.";

    private final UserSubtypeService userSubtypeService;
    private final DaoAccessorService daoAccessorService;
    private final NotificationService notificationService;
    private final MessagePublisher messagePublisher;
    private final MediaService mediaService;

    public MessagesBusiness(DaoAccessorService daoAccessorService,
                            NotificationService notificationService,
                            MessagePublisher messagePublisher,
                            MediaService mediaService,
            UserSubtypeService userSubtypeService) {
        this.userSubtypeService = userSubtypeService;
        this.daoAccessorService = daoAccessorService;
        this.notificationService = notificationService;
        this.messagePublisher = messagePublisher;
        this.mediaService = mediaService;
    }

    // ── Appelant ─────────────────────────────────────────────────────────────

    private UtilisateursEntity appelant() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null || "anonymousUser".equals(auth.getName())) {
            throw new SchoolException(SchoolErrorCode.OPERATION_INTERDITE, "Authentification requise");
        }
        return users().findByEmail(auth.getName())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.OPERATION_INTERDITE, "Utilisateur authentifié introuvable"));
    }

    private boolean estAdmin(UtilisateursEntity u) {
        if (u != null && Boolean.TRUE.equals(u.getAdmin())) return true;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && u != null && u.getEmail() != null && u.getEmail().equals(auth.getName())
                && auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    /** The {utilisateurId} of a path must be the caller (admins may read anyone's). */
    private UtilisateursEntity verifierSoiOuAdmin(String utilisateurId) {
        UtilisateursEntity moi = appelant();
        if (!moi.getId().equals(utilisateurId) && !estAdmin(moi)) {
            throw new SchoolException(SchoolErrorCode.OPERATION_INTERDITE,
                    "Vous ne pouvez consulter que vos propres messages");
        }
        if (!moi.getId().equals(utilisateurId) && !users().existsById(utilisateurId)) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable");
        }
        return moi;
    }

    private UtilisateursEntity verifierSoi(String utilisateurId) {
        UtilisateursEntity moi = appelant();
        if (!moi.getId().equals(utilisateurId)) {
            throw new SchoolException(SchoolErrorCode.OPERATION_INTERDITE,
                    "Vous ne pouvez modifier que vos propres messages");
        }
        return moi;
    }

    private static boolean estExpediteur(MessagesEntity m, String userId) {
        return m.getExpediteurEntity() != null && m.getExpediteurEntity().getId().equals(userId);
    }

    private static boolean estDestinataire(MessagesEntity m, String userId) {
        return m.getDestinatairesEntities() != null
                && m.getDestinatairesEntities().stream().anyMatch(d -> d != null && userId.equals(d.getId()));
    }

    private void verifierParticipant(MessagesEntity m, String userId) {
        if (!estExpediteur(m, userId) && !estDestinataire(m, userId)) {
            throw new SchoolException(SchoolErrorCode.OPERATION_INTERDITE,
                    "Vous n'êtes ni l'expéditeur ni un destinataire de ce message");
        }
    }

    // ── Qui peut écrire à qui ────────────────────────────────────────────────

    private class Autorisations {
        final UtilisateursEntity expediteur;
        final boolean admin;
        final Set<String> classesEffectives;
        final Set<String> famille;
        final Map<String, Set<String>> cache = new HashMap<>();

        Autorisations(UtilisateursEntity expediteur) {
            this.expediteur = expediteur;
            this.admin = estAdmin(expediteur);
            this.classesEffectives = admin ? Set.of() : new HashSet<>(messages().findEffectiveClasseIds(expediteur.getId()));
            this.famille = admin ? Set.of() : new HashSet<>(messages().findFamilyIds(expediteur.getId()));
        }

        boolean peutEcrireA(UtilisateursEntity dest) {
            if (admin || Boolean.TRUE.equals(dest.getAdmin())) return true;
            if (dest.getId().equals(expediteur.getId())) return true;
            if (famille.contains(dest.getId())) return true;
            if (classesEffectives.isEmpty()) return false;
            Set<String> classesDest = cache.computeIfAbsent(dest.getId(),
                    id -> new HashSet<>(messages().findEffectiveClasseIds(id)));
            return classesDest.stream().anyMatch(classesEffectives::contains);
        }
    }

    private void verifierDestinataires(Autorisations auth, Collection<UtilisateursEntity> destinataires) {
        List<String> refuses = destinataires.stream()
                .filter(d -> !auth.peutEcrireA(d))
                .map(d -> (Objects.toString(d.getPrenom(), "") + " " + Objects.toString(d.getNom(), "")).trim())
                .collect(Collectors.toList());
        if (!refuses.isEmpty()) {
            throw new SchoolException(SchoolErrorCode.OPERATION_INTERDITE,
                    MSG_INTERDIT_DESTINATAIRE + " Destinataire(s) non autorisé(s) : " + String.join(", ", refuses));
        }
    }

    /** Users the caller may write to (recipient picker). */
    public List<UtilisateurSimpleDto> obtenirContacts() {
        UtilisateursEntity moi = appelant();
        List<UtilisateursEntity> contacts;
        if (estAdmin(moi)) {
            contacts = users().findAll();
        } else {
            Set<String> ids = new HashSet<>();
            List<String> classes = messages().findEffectiveClasseIds(moi.getId());
            if (!classes.isEmpty()) ids.addAll(messages().findContactIdsForClasses(classes));
            ids.addAll(messages().findFamilyIds(moi.getId()));
            ids.addAll(users().findAdminUserIds());
            contacts = users().findAllById(ids);
        }
        return contacts.stream()
                .filter(u -> u != null && !u.getId().equals(moi.getId()))
                .map(this::mapToUtilisateurSimpleDto)
                .sorted(Comparator.comparing((UtilisateurSimpleDto u) -> Objects.toString(u.getNom(), "").toLowerCase())
                        .thenComparing(u -> Objects.toString(u.getPrenom(), "").toLowerCase()))
                .collect(Collectors.toList());
    }

    /** Classes the caller may send a group message to. */
    public List<MessageClasseDto> obtenirClassesAutorisees() {
        UtilisateursEntity moi = appelant();
        List<ClassesEntity> classes = estAdmin(moi)
                ? daoAccessorService.getRepository(ClassesRepository.class).findAll()
                : daoAccessorService.getRepository(ClassesRepository.class).findAllById(messages().findLinkedClasseIds(moi.getId()));
        return classes.stream()
                .map(c -> new MessageClasseDto(c.getId(), c.getNom(), c.getNiveau()))
                .sorted(Comparator.comparing(c -> Objects.toString(c.getNom(), "").toLowerCase()))
                .collect(Collectors.toList());
    }

    // ── Lecture ──────────────────────────────────────────────────────────────

    public Messages avoirMessage(String idMessage) {
        UtilisateursEntity moi = appelant();
        MessagesEntity messageEntity = trouverMessage(idMessage);
        if (!estAdmin(moi)) verifierParticipant(messageEntity, moi.getId());
        return mapMessageEntityToDto(messageEntity);
    }

    public List<Messages> avoirToutMessages() {
        if (!estAdmin(appelant())) {
            throw new SchoolException(SchoolErrorCode.OPERATION_INTERDITE, "Réservé aux administrateurs");
        }
        return messages().findAll().stream()
                .map(this::mapMessageEntityToDto)
                .collect(Collectors.toList());
    }

    public List<Messages> obtenirMessagesParUtilisateur(String utilisateurId) {
        verifierSoiOuAdmin(utilisateurId);
        Set<String> masques = idsMasques(utilisateurId);
        List<MessagesEntity> all = new ArrayList<>(messages().findByExpediteurEntityId(utilisateurId));
        all.addAll(messages().findByDestinatairesEntitiesId(utilisateurId));
        return all.stream()
                .filter(m -> !masques.contains(m.getId()))
                .map(this::mapMessageEntityToDto)
                .collect(Collectors.toList());
    }

    public List<MessageDto> obtenirMessagesEnvoyes(String utilisateurId) {
        verifierSoiOuAdmin(utilisateurId);
        Map<String, MessageStatutEntity> statuts = statutsDe(utilisateurId);
        return messages().findByExpediteurEntityId(utilisateurId).stream()
                .filter(m -> !estMasque(statuts.get(m.getId())))
                .map(m -> mapToMessageDto(m, statuts.get(m.getId()), true))
                .collect(Collectors.toList());
    }

    public List<MessageDto> obtenirMessagesRecus(String utilisateurId) {
        verifierSoiOuAdmin(utilisateurId);
        Map<String, MessageStatutEntity> statuts = statutsDe(utilisateurId);
        return messages().findByDestinatairesEntitiesId(utilisateurId).stream()
                .filter(m -> !estMasque(statuts.get(m.getId())))
                .map(m -> mapToMessageDto(m, statuts.get(m.getId()), estExpediteur(m, utilisateurId)))
                .collect(Collectors.toList());
    }

    // ── Envoi ────────────────────────────────────────────────────────────────

    public Messages posterMessage(Messages message) {
        UtilisateursEntity expediteur = appelant();

        List<String> ids = message.getDestinataires() == null ? List.of() : message.getDestinataires().stream()
                .filter(Objects::nonNull).map(Utilisateurs::getId).filter(Objects::nonNull)
                .distinct().collect(Collectors.toList());
        if (ids.isEmpty()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Au moins un destinataire est requis");
        }
        List<UtilisateursEntity> destinataires = users().findAllById(ids);
        if (destinataires.size() != ids.size()) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Un ou plusieurs destinataires sont introuvables");
        }
        verifierDestinataires(new Autorisations(expediteur), destinataires);

        MessagesEntity entity = nouveauMessage(expediteur, message.getObjet(), message.getContenu(), message.getMedias());
        entity.setDestinatairesEntities(new ArrayList<>(destinataires));
        return enregistrerEtDiffuser(entity, expediteur);
    }

    public Messages posterMessageGroupe(GroupMessageDto dto) {
        UtilisateursEntity expediteur = appelant();
        if (dto.getClassIds() == null || dto.getClassIds().isEmpty()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Au moins une classe est requise");
        }

        Autorisations auth = new Autorisations(expediteur);
        Set<String> classesLiees = auth.admin ? Set.of() : new HashSet<>(messages().findLinkedClasseIds(expediteur.getId()));

        List<ClassesEntity> classes = new ArrayList<>();
        for (String classId : new LinkedHashSet<>(dto.getClassIds())) {
            ClassesEntity classe = daoAccessorService.getRepository(ClassesRepository.class).findById(classId)
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe non trouvée avec l'ID: " + classId));
            if (!auth.admin && !classesLiees.contains(classId)) {
                throw new SchoolException(SchoolErrorCode.OPERATION_INTERDITE,
                        "Vous devez être membre ou modérateur de la classe « " + classe.getNom() + " » pour lui envoyer un message");
            }
            classes.add(classe);
        }

        Map<String, UtilisateursEntity> destinataires = new LinkedHashMap<>();
        for (ClassesEntity classe : classes) {
            daoAccessorService.getRepository(AccederRepository.class).findByClasseId(classe.getId()).stream()
                    .map(AccederEntity::getUtilisateur)
                    .filter(Objects::nonNull)
                    .forEach(u -> destinataires.putIfAbsent(u.getId(), u));
            if (classe.getModerator() != null) {
                destinataires.putIfAbsent(classe.getModerator().getId(), classe.getModerator());
            }
        }

        if (dto.getCopieRecipientIds() != null && !dto.getCopieRecipientIds().isEmpty()) {
            List<String> copieIds = dto.getCopieRecipientIds().stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
            List<UtilisateursEntity> copies = users().findAllById(copieIds);
            if (copies.size() != copieIds.size()) {
                throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Un ou plusieurs destinataires en copie sont introuvables");
            }
            verifierDestinataires(auth, copies);
            copies.forEach(u -> destinataires.putIfAbsent(u.getId(), u));
        }
        destinataires.remove(expediteur.getId());
        if (destinataires.isEmpty()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Aucun destinataire dans les classes sélectionnées");
        }

        MessagesEntity entity = nouveauMessage(expediteur, dto.getObjet(), dto.getContent(), dto.getMedias());
        entity.setDestinatairesEntities(new ArrayList<>(destinataires.values()));
        entity.setClasses(classes);
        return enregistrerEtDiffuser(entity, expediteur);
    }

    private MessagesEntity nouveauMessage(UtilisateursEntity expediteur, String objet, String contenu, List<Media> medias) {
        String texte = contenu == null ? "" : contenu.trim();
        List<Media> pieces = medias == null ? List.of() : medias.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (texte.isEmpty() && pieces.isEmpty()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Le message doit contenir du texte ou au moins une pièce jointe");
        }
        if (pieces.size() > MAX_MEDIAS) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Au plus " + MAX_MEDIAS + " pièces jointes par message");
        }

        MessagesEntity entity = new MessagesEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setExpediteurEntity(expediteur);
        entity.setContenu(texte);
        entity.setObjet(objetParDefaut(objet, texte));
        // ISO-8601: parseable by every client (Date#toString() is not)
        entity.setDateCreation(ServerDateTimes.nowIso());
        entity.setEtat("envoyé");

        int ordre = 0;
        for (Media media : pieces) {
            entity.getPiecesJointesEntities().add(construirePieceJointe(media, entity, expediteur.getId(), ordre++));
        }
        return entity;
    }

    private static String objetParDefaut(String objet, String texte) {
        if (objet != null && !objet.isBlank()) {
            return objet.length() > 255 ? objet.substring(0, 255) : objet.trim();
        }
        if (texte.isEmpty()) return "Pièce jointe";
        String ligne = texte.split("\\R", 2)[0].trim();
        return ligne.length() > 60 ? ligne.substring(0, 57) + "..." : ligne;
    }

    private MessageMediaEntity construirePieceJointe(Media media, MessagesEntity message, String expediteurId, int ordre) {
        String filePath = normaliserCheminMedia(media.getFilePath());
        if (filePath == null || !filePath.startsWith("users/" + expediteurId + "/")) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "Pièce jointe invalide : le fichier doit avoir été téléversé par l'expéditeur");
        }
        String contentType = media.getContentType() != null && !media.getContentType().isBlank()
                ? media.getContentType() : "application/octet-stream";
        String fileName = media.getFileName() != null && !media.getFileName().isBlank()
                ? media.getFileName() : filePath.substring(filePath.lastIndexOf('/') + 1);

        MessageMediaEntity e = new MessageMediaEntity();
        e.setId(UUID.randomUUID().toString());
        e.setMessage(message);
        e.setFilePath(filePath);
        e.setFileName(fileName.length() > 255 ? fileName.substring(0, 255) : fileName);
        e.setContentType(contentType.length() > 150 ? contentType.substring(0, 150) : contentType);
        e.setMediaType(typeMedia(contentType));
        e.setFileSize(media.getFileSize());
        e.setOrdre(ordre);
        e.setDateCreation(LocalDateTime.now());
        return e;
    }

    /** Accepts a storage key, or the full (presigned) upload URL from which the key is extracted. */
    private static String normaliserCheminMedia(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String path = raw.trim();
        int q = path.indexOf('?');
        if (q >= 0) path = path.substring(0, q);
        if (path.startsWith("http://") || path.startsWith("https://")) {
            int idx = path.indexOf("/users/");
            if (idx < 0) return null;
            path = path.substring(idx + 1);
        }
        while (path.startsWith("/")) path = path.substring(1);
        try {
            path = java.net.URLDecoder.decode(path, java.nio.charset.StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ignored) {
            // keep as-is
        }
        return path.contains("..") ? null : path;
    }

    private static String typeMedia(String contentType) {
        String ct = contentType.toLowerCase(Locale.ROOT);
        if (ct.startsWith("image/")) return "IMAGE";
        if (ct.startsWith("video/")) return "VIDEO";
        return "DOCUMENT";
    }

    private Messages enregistrerEtDiffuser(MessagesEntity entity, UtilisateursEntity expediteur) {
        MessagesEntity saved = messages().save(entity);

        try {
            String senderName = Objects.toString(expediteur.getPrenom(), "") + " " + Objects.toString(expediteur.getNom(), "");
            for (UtilisateursEntity dest : saved.getDestinatairesEntities()) {
                notificationService.createMessageNotification(dest.getId(), expediteur.getId(), senderName.trim(), saved.getObjet());
            }
        } catch (Exception e) {
            log.error("Failed to send message notifications: {}", e.getMessage());
        }

        try {
            messagePublisher.pushNewMessage(mapToMessageDto(saved, null, true), expediteur.getId());
        } catch (Exception e) {
            log.warn("Failed to push real-time message event: {}", e.getMessage());
        }

        return mapMessageEntityToDto(saved);
    }

    // ── Suppression / corbeille (par utilisateur) ────────────────────────────

    public void supprimerMessage(String messageId, String scope) {
        UtilisateursEntity moi = appelant();
        supprimerPour(moi, trouverMessage(messageId), scope, true);
    }

    public Map<String, Integer> supprimerMessages(MessageBulkDeleteRequest request) {
        UtilisateursEntity moi = appelant();
        if (request == null || request.getMessageIds() == null || request.getMessageIds().isEmpty()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Aucun message à supprimer");
        }
        int n = 0;
        for (String id : new LinkedHashSet<>(request.getMessageIds())) {
            Optional<MessagesEntity> m = messages().findById(id);
            if (m.isEmpty()) continue;
            supprimerPour(moi, m.get(), request.getScope(), false);
            n++;
        }
        return Map.of("deleted", n);
    }

    /**
     * @param strict when true, scope=everyone by a non-sender is refused (single delete);
     *               when false (bulk), it falls back to "for me" for messages the caller did not send.
     */
    private void supprimerPour(UtilisateursEntity moi, MessagesEntity m, String scope, boolean strict) {
        verifierParticipant(m, moi.getId());
        boolean pourTous = SCOPE_EVERYONE.equalsIgnoreCase(scope);
        boolean expediteur = estExpediteur(m, moi.getId());

        if (pourTous && !expediteur && strict) {
            throw new SchoolException(SchoolErrorCode.OPERATION_INTERDITE,
                    "Seul l'expéditeur peut supprimer un message pour tout le monde");
        }

        if (pourTous && expediteur) {
            if (!m.isDeleted()) {
                m.setEtatOriginal(m.getEtat());
                m.setDeleted(true);
                m.setDateSuppression(ServerDateTimes.nowIso());
                m.setEtat("supprimé");
                messages().save(m);
            }
            List<String> participants = new ArrayList<>();
            participants.add(moi.getId());
            m.getDestinatairesEntities().forEach(d -> participants.add(d.getId()));
            messagePublisher.pushDeleted(m.getId(), participants);
            return;
        }

        MessageStatutEntity statut = getOrCreateStatut(moi.getId(), m.getId());
        statut.setSupprime(true);
        statut.setPurge(false);
        statut.setDateSuppression(new Date());
        statuts().save(statut);
        messagePublisher.pushDeleted(m.getId(), List.of(moi.getId()));
    }

    public List<MessageDto> obtenirMessagesCorbeille(String utilisateurId) {
        verifierSoiOuAdmin(utilisateurId);
        return corbeille(utilisateurId).stream()
                .map(m -> {
                    MessageStatutEntity s = statuts().findById(new MessageStatutId(utilisateurId, m.getId())).orElse(null);
                    MessageDto dto = mapToMessageDto(m, s, estExpediteur(m, utilisateurId));
                    dto.setSupprimePourTous(m.isDeleted());
                    if (s != null && s.isSupprime() && s.getDateSuppression() != null) {
                        dto.setDateSuppression(ServerDateTimes.format(s.getDateSuppression()));
                    } else {
                        dto.setDateSuppression(ServerDateTimes.normalize(m.getDateSuppression()));
                    }
                    return dto;
                })
                .sorted(Comparator.comparing((MessageDto d) -> Objects.toString(d.getDateSuppression(), "")).reversed())
                .collect(Collectors.toList());
    }

    /** Messages currently in the user's trash. */
    private List<MessagesEntity> corbeille(String utilisateurId) {
        Map<String, MessageStatutEntity> statuts = statutsDe(utilisateurId);
        Map<String, MessagesEntity> result = new LinkedHashMap<>();

        // deleted for everyone by me (sender)
        messages().findByExpediteurEntityIdAndDeleted(utilisateurId, true).stream()
                .filter(m -> { MessageStatutEntity s = statuts.get(m.getId()); return s == null || !s.isPurge(); })
                .forEach(m -> result.put(m.getId(), m));

        // deleted for me (unless the sender since removed it for everyone and I'm not the sender)
        statuts.values().stream()
                .filter(s -> s.isSupprime() && !s.isPurge())
                .map(MessageStatutEntity::getMessage)
                .filter(Objects::nonNull)
                .filter(m -> !m.isDeleted() || estExpediteur(m, utilisateurId))
                .forEach(m -> result.putIfAbsent(m.getId(), m));

        return new ArrayList<>(result.values());
    }

    /** Permanently removes the caller's trash entries only. */
    public void viderCorbeille() {
        UtilisateursEntity moi = appelant();
        List<MessagesEntity> items = corbeille(moi.getId());
        for (MessagesEntity m : items) {
            MessageStatutEntity s = getOrCreateStatut(moi.getId(), m.getId());
            s.setSupprime(true);
            s.setPurge(true);
            if (s.getDateSuppression() == null) s.setDateSuppression(new Date());
            statuts().save(s);
        }
        for (MessagesEntity m : items) {
            supprimerDefinitivementSiPlusPersonne(m);
        }
        log.info("Corbeille vidée pour {} : {} message(s)", moi.getId(), items.size());
    }

    /**
     * Deletes the message row (and its attachments) once nobody can see it any more:
     * the sender purged it and either it was deleted for everyone or every recipient purged it.
     */
    private void supprimerDefinitivementSiPlusPersonne(MessagesEntity m) {
        Map<String, MessageStatutEntity> parUser = statuts().findByIdMessageId(m.getId()).stream()
                .collect(Collectors.toMap(s -> s.getId().getUtilisateurId(), Function.identity(), (a, b) -> a));
        Function<String, Boolean> purge = uid -> parUser.containsKey(uid) && parUser.get(uid).isPurge();

        boolean expediteurPurge = m.getExpediteurEntity() == null || purge.apply(m.getExpediteurEntity().getId());
        boolean destinatairesPurge = m.isDeleted()
                || m.getDestinatairesEntities().stream().allMatch(d -> purge.apply(d.getId()));
        if (!expediteurPurge || !destinatairesPurge) return;
        if (messages().countInteractionsByMessageId(m.getId()) > 0) return;

        List<String> chemins = m.getPiecesJointesEntities().stream().map(MessageMediaEntity::getFilePath).collect(Collectors.toList());
        statuts().deleteAll(parUser.values());
        messages().delete(m);

        for (String chemin : chemins) {
            try {
                daoAccessorService.getRepository(MediaRepository.class).findByFilePath(chemin)
                        .ifPresent(media -> daoAccessorService.getRepository(MediaRepository.class).delete(media));
                mediaService.deleteMedia(chemin);
            } catch (Exception e) {
                log.warn("Could not delete stored attachment {}: {}", chemin, e.getMessage());
            }
        }
    }

    public void restaurerMessage(String messageId) {
        UtilisateursEntity moi = appelant();
        MessagesEntity m = trouverMessage(messageId);
        verifierParticipant(m, moi.getId());

        Optional<MessageStatutEntity> statut = statuts().findById(new MessageStatutId(moi.getId(), messageId));
        boolean restaure = false;

        if (estExpediteur(m, moi.getId()) && m.isDeleted() && statut.map(s -> !s.isPurge()).orElse(true)) {
            m.setDeleted(false);
            m.setDateSuppression(null);
            m.setEtat(m.getEtatOriginal() != null ? m.getEtatOriginal() : "envoyé");
            m.setEtatOriginal(null);
            messages().save(m);
            restaure = true;
            // visible again for recipients who had not deleted it themselves
            Map<String, MessageStatutEntity> parUser = statuts().findByIdMessageId(m.getId()).stream()
                    .collect(Collectors.toMap(s -> s.getId().getUtilisateurId(), Function.identity(), (a, b) -> a));
            for (UtilisateursEntity d : m.getDestinatairesEntities()) {
                MessageStatutEntity sd = parUser.get(d.getId());
                if (!estMasque(sd)) messagePublisher.pushRestored(mapToMessageDto(m, sd, false), d.getId());
            }
        }

        if (statut.isPresent() && statut.get().isSupprime() && !statut.get().isPurge()) {
            MessageStatutEntity s = statut.get();
            s.setSupprime(false);
            s.setDateSuppression(null);
            statuts().save(s);
            restaure = true;
        }

        if (!restaure) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Le message n'est pas dans votre corbeille");
        }
        messagePublisher.pushRestored(mapToMessageDto(m, statut.orElse(null), estExpediteur(m, moi.getId())), moi.getId());
    }

    // ── Statuts lu / favori ──────────────────────────────────────────────────

    public MessageStatutDTO marquerLu(String utilisateurId, String messageId, boolean lu) {
        verifierSoi(utilisateurId);
        verifierParticipant(trouverMessage(messageId), utilisateurId);
        MessageStatutEntity statut = getOrCreateStatut(utilisateurId, messageId);
        statut.setLu(lu);
        statut.setDateLecture(lu ? new Date() : null);
        statuts().save(statut);
        return mapStatutToDto(statut);
    }

    public MessageStatutDTO marquerFavori(String utilisateurId, String messageId, boolean favori) {
        verifierSoi(utilisateurId);
        verifierParticipant(trouverMessage(messageId), utilisateurId);
        MessageStatutEntity statut = getOrCreateStatut(utilisateurId, messageId);
        statut.setFavori(favori);
        statuts().save(statut);
        return mapStatutToDto(statut);
    }

    public MessageStatutDTO obtenirStatut(String utilisateurId, String messageId) {
        verifierSoiOuAdmin(utilisateurId);
        return statuts().findById(new MessageStatutId(utilisateurId, messageId))
                .map(this::mapStatutToDto)
                .orElse(MessageStatutDTO.builder()
                        .messageId(messageId)
                        .utilisateurId(utilisateurId)
                        .lu(false)
                        .favori(false)
                        .build());
    }

    public List<MessageStatutDTO> obtenirFavoris(String utilisateurId) {
        verifierSoiOuAdmin(utilisateurId);
        return statuts().findByIdUtilisateurIdAndFavoriTrue(utilisateurId).stream()
                .filter(s -> !s.isSupprime())
                .map(this::mapStatutToDto).collect(Collectors.toList());
    }

    /** Unread = received, visible messages without a lu=true status. */
    public long compterNonLus(String utilisateurId) {
        verifierSoiOuAdmin(utilisateurId);
        return messagesRecusNonLus(utilisateurId).size();
    }

    public List<MessageStatutDTO> obtenirNonLus(String utilisateurId) {
        verifierSoiOuAdmin(utilisateurId);
        return messagesRecusNonLus(utilisateurId).stream()
                .map(m -> statuts().findById(new MessageStatutId(utilisateurId, m.getId()))
                        .map(this::mapStatutToDto)
                        .orElse(MessageStatutDTO.builder().messageId(m.getId()).utilisateurId(utilisateurId).build()))
                .collect(Collectors.toList());
    }

    private List<MessagesEntity> messagesRecusNonLus(String utilisateurId) {
        Map<String, MessageStatutEntity> statuts = statutsDe(utilisateurId);
        return messages().findByDestinatairesEntitiesId(utilisateurId).stream()
                .filter(m -> !estExpediteur(m, utilisateurId))
                .filter(m -> {
                    MessageStatutEntity s = statuts.get(m.getId());
                    return s == null || (!s.isLu() && !s.isSupprime());
                })
                .collect(Collectors.toList());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private MessagesRepository messages() {
        return daoAccessorService.getRepository(MessagesRepository.class);
    }

    private MessageStatutRepository statuts() {
        return daoAccessorService.getRepository(MessageStatutRepository.class);
    }

    private UtilisateursRepository users() {
        return daoAccessorService.getRepository(UtilisateursRepository.class);
    }

    private MessagesEntity trouverMessage(String id) {
        return messages().findById(id)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Message introuvable avec l'ID: " + id));
    }

    private Map<String, MessageStatutEntity> statutsDe(String utilisateurId) {
        return statuts().findByIdUtilisateurId(utilisateurId).stream()
                .collect(Collectors.toMap(s -> s.getId().getMessageId(), Function.identity(), (a, b) -> a));
    }

    private Set<String> idsMasques(String utilisateurId) {
        return statuts().findByIdUtilisateurIdAndSupprimeTrue(utilisateurId).stream()
                .map(s -> s.getId().getMessageId()).collect(Collectors.toSet());
    }

    private static boolean estMasque(MessageStatutEntity s) {
        return s != null && (s.isSupprime() || s.isPurge());
    }

    private MessageStatutEntity getOrCreateStatut(String utilisateurId, String messageId) {
        MessageStatutId id = new MessageStatutId(utilisateurId, messageId);
        return statuts().findById(id).orElseGet(() -> {
            MessageStatutEntity s = new MessageStatutEntity();
            s.setId(id);
            s.setUtilisateur(users().findById(utilisateurId)
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable")));
            s.setMessage(trouverMessage(messageId));
            return s;
        });
    }

    private MessageDto mapToMessageDto(MessagesEntity entity, MessageStatutEntity statut, boolean isSender) {
        MessageDto dto = new MessageDto();
        dto.setId(entity.getId());
        dto.setObjet(entity.getObjet());
        dto.setContenu(entity.getContenu());
        dto.setDateCreation(ServerDateTimes.normalize(entity.getDateCreation()));
        dto.setDateModification(ServerDateTimes.normalize(entity.getDateModification()));
        dto.setEtat(entity.getEtat());

        if (entity.getExpediteurEntity() != null) {
            dto.setExpediteur(mapToUtilisateurSimpleDto(entity.getExpediteurEntity()));
        }
        if (entity.getDestinatairesEntities() != null) {
            dto.setDestinataires(entity.getDestinatairesEntities().stream()
                    .filter(Objects::nonNull)
                    .map(this::mapToUtilisateurSimpleDto)
                    .collect(Collectors.toList()));
        }
        try {
            dto.setClasseIds(entity.getClasses() == null ? new ArrayList<>()
                    : entity.getClasses().stream().map(ClassesEntity::getId).collect(Collectors.toList()));
        } catch (Exception e) {
            dto.setClasseIds(new ArrayList<>());
        }

        if (statut != null) {
            dto.setLu(statut.isLu() || isSender);
            dto.setFavori(statut.isFavori());
            dto.setDateLecture(statut.getDateLecture());
        } else {
            // a sender has inherently "read" their own message
            dto.setLu(isSender);
        }
        dto.setSupprimePourTous(entity.isDeleted());
        dto.setMedias(mapMedias(entity));
        return dto;
    }

    private List<Media> mapMedias(MessagesEntity entity) {
        if (entity.getPiecesJointesEntities() == null) return new ArrayList<>();
        return entity.getPiecesJointesEntities().stream().map(pj -> {
            Media m = new Media();
            m.setId(pj.getId());
            m.setFileName(pj.getFileName());
            m.setFilePath(pj.getFilePath());
            m.setContentType(pj.getContentType());
            m.setMediaType(pj.getMediaType());
            m.setFileType(pj.getMediaType());
            m.setFileSize(pj.getFileSize());
            m.setUploadedDate(pj.getDateCreation());
            m.setOwnerId(entity.getExpediteurEntity() != null ? entity.getExpediteurEntity().getId() : null);
            try {
                m.setPresignedUrl(mediaService.generateDownloadPresignedUrl(pj.getFilePath()));
            } catch (Exception ex) {
                log.warn("Could not generate presigned URL for message media {}: {}", pj.getId(), ex.getMessage());
            }
            return m;
        }).collect(Collectors.toList());
    }

    private Messages mapMessageEntityToDto(MessagesEntity entity) {
        Messages message = new Messages();
        message.setId(entity.getId());
        message.setObjet(entity.getObjet());
        message.setContenu(entity.getContenu());
        message.setDateCreation(ServerDateTimes.normalize(entity.getDateCreation()));
        message.setDateModification(ServerDateTimes.normalize(entity.getDateModification()));
        message.setEtat(entity.getEtat());
        if (entity.getDestinatairesEntities() != null) {
            message.setDestinataires(entity.getDestinatairesEntities().stream()
                    .filter(Objects::nonNull)
                    .map(this::mapUtilisateursEntityToModele)
                    .collect(Collectors.toList()));
        }
        if (entity.getExpediteurEntity() != null) {
            message.setExpediteur(mapUtilisateursEntityToModele(entity.getExpediteurEntity()));
        }
        message.setMedias(mapMedias(entity));
        return message;
    }

    private UtilisateurSimpleDto mapToUtilisateurSimpleDto(UtilisateursEntity entity) {
        UtilisateurSimpleDto dto = new UtilisateurSimpleDto();
        dto.setId(entity.getId());
        dto.setNom(entity.getNom());
        dto.setPrenom(entity.getPrenom());
        dto.setEmail(entity.getEmail());
        if (Boolean.TRUE.equals(entity.getAdmin())) dto.setTypeUtilisateur("ADMIN");
        else {
            // Rôle lu dans les tables filles (le sous-type chargé est arbitraire pour un compte multi-rôles)
            String type = userSubtypeService.typeUtilisateur(entity.getId());
            dto.setTypeUtilisateur(type == null || "GESTIONNAIRE".equals(type) ? "UTILISATEUR" : type);
        }
        return dto;
    }

    private MessageStatutDTO mapStatutToDto(MessageStatutEntity e) {
        return MessageStatutDTO.builder()
                .messageId(e.getId().getMessageId())
                .utilisateurId(e.getId().getUtilisateurId())
                .lu(e.isLu())
                .favori(e.isFavori())
                .dateLecture(e.getDateLecture())
                .build();
    }

    private Utilisateurs mapUtilisateursEntityToModele(UtilisateursEntity entity) {
        String type = userSubtypeService.typeUtilisateur(entity.getId());
        if ("PROFESSEUR".equals(type)) {
            return dozerMapperBean.map(entity, Professeurs.class);
        } else if ("ELEVE".equals(type)) {
            return dozerMapperBean.map(entity, Eleves.class);
        } else if ("REPETITEUR".equals(type)) {
            return dozerMapperBean.map(entity, Repetiteurs.class);
        } else if ("PARENT".equals(type)) {
            return dozerMapperBean.map(entity, Parents.class);
        } else {
            return dozerMapperBean.map(entity, Utilisateurs.class);
        }
    }
}
