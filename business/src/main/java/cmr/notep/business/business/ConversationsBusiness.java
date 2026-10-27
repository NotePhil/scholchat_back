package cmr.notep.business.business;

import cmr.notep.business.config.time.ServerDateTimes;
import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.security.UserSubtypeService;
import cmr.notep.business.services.MediaService;
import cmr.notep.business.services.MessagePublisher;
import cmr.notep.interfaces.dto.ChatMessageDto;
import cmr.notep.interfaces.dto.ChatMessagePageDto;
import cmr.notep.interfaces.dto.ConversationDto;
import cmr.notep.interfaces.modeles.Media;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.*;
import cmr.notep.ressourcesjpa.repository.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Messagerie de type chat (WhatsApp) : conversations DIRECT (une seule par paire) et GROUPE.
 *
 * <ul>
 *   <li>seuls les participants voient une conversation et ses messages ;</li>
 *   <li>non lus = messages des autres postérieurs à {@code last_read_at}, ni supprimés, ni marqués lus
 *       (message_statut, pour rester cohérent avec les anciens clients) ;</li>
 *   <li>« supprimer la conversation pour moi » ({@code deleted_at}) met la conversation dans la corbeille
 *       de l'utilisateur : masquée jusqu'au prochain message, historique antérieur caché tant qu'elle n'est
 *       pas restaurée ; la suppression définitive ({@code historique_depuis}) cache cet historique pour de bon ;</li>
 *   <li>favori, « marquer comme non lu » et corbeille sont propres à chaque participant ;</li>
 *   <li>les règles d'envoi (qui peut écrire à qui) et de suppression des messages restent celles de
 *       {@link MessagesBusiness}.</li>
 * </ul>
 */
@Component
@Slf4j
@Transactional
public class ConversationsBusiness {

    public static final String PIECE_JOINTE = "📎 Pièce jointe";
    private static final int EXTRAIT_MAX = 100;
    private static final LocalDateTime ORIGINE = LocalDateTime.of(1970, 1, 1, 0, 0);

    private final DaoAccessorService daoAccessorService;
    private final UserSubtypeService userSubtypeService;
    private final MediaService mediaService;
    private final MessagePublisher messagePublisher;

    public ConversationsBusiness(DaoAccessorService daoAccessorService, UserSubtypeService userSubtypeService,
                                 MediaService mediaService, MessagePublisher messagePublisher) {
        this.daoAccessorService = daoAccessorService;
        this.userSubtypeService = userSubtypeService;
        this.mediaService = mediaService;
        this.messagePublisher = messagePublisher;
    }

    // ── Appelant / accès ─────────────────────────────────────────────────────

    public UtilisateursEntity appelant() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null || "anonymousUser".equals(auth.getName())) {
            throw new SchoolException(SchoolErrorCode.OPERATION_INTERDITE, "Authentification requise");
        }
        return users().findByEmail(auth.getName())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.OPERATION_INTERDITE, "Utilisateur authentifié introuvable"));
    }

    public ConversationEntity trouver(String conversationId) {
        return conversations().findById(conversationId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Conversation introuvable"));
    }

    /** The conversation, if {@code userId} is one of its participants (403 otherwise). */
    public ConversationEntity verifierParticipant(String conversationId, String userId) {
        ConversationEntity c = trouver(conversationId);
        participant(conversationId, userId);
        return c;
    }

    public ConversationParticipantEntity participant(String conversationId, String userId) {
        return participants().findById(new ConversationParticipantId(conversationId, userId))
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.OPERATION_INTERDITE,
                        "Vous ne participez pas à cette conversation"));
    }

    public List<ConversationParticipantEntity> participantsDe(String conversationId) {
        return participants().findByIdConversationId(conversationId);
    }

    // ── Création / rattachement ──────────────────────────────────────────────

    static String directKey(String a, String b) {
        return a.compareTo(b) <= 0 ? a + "|" + b : b + "|" + a;
    }

    static String participantsKey(Collection<String> ids) {
        String joined = ids.stream().filter(Objects::nonNull).distinct().sorted().collect(Collectors.joining("|"));
        return DigestUtils.md5DigestAsHex(joined.getBytes(StandardCharsets.UTF_8));
    }

    /** Find-or-create the DIRECT conversation of a pair (race-safe thanks to the unique direct_key). */
    public ConversationEntity trouverOuCreerDirect(String moiId, String autreId) {
        String key = directKey(moiId, autreId);
        Optional<ConversationEntity> existante = conversations().findByDirectKey(key);
        ConversationEntity c;
        if (existante.isPresent()) {
            c = existante.get();
        } else {
            conversations().insererDirectSiAbsent(UUID.randomUUID().toString(), moiId, maintenant(), key,
                    participantsKey(List.of(moiId, autreId)));
            c = conversations().findByDirectKey(key)
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Conversation introuvable"));
        }
        participants().ajouterSiAbsent(c.getId(), moiId, c.getCreatedAt());
        participants().ajouterSiAbsent(c.getId(), autreId, c.getCreatedAt());
        return c;
    }

    /** New GROUPE conversation; {@code membres} includes the creator. */
    public ConversationEntity creerGroupe(UtilisateursEntity createur, Collection<UtilisateursEntity> membres, String titre, String classeId) {
        Map<String, UtilisateursEntity> parId = new LinkedHashMap<>();
        parId.put(createur.getId(), createur);
        membres.stream().filter(Objects::nonNull).forEach(u -> parId.putIfAbsent(u.getId(), u));

        ConversationEntity c = new ConversationEntity();
        c.setId(UUID.randomUUID().toString());
        c.setType(ConversationEntity.GROUPE);
        c.setTitre(titreGroupe(titre, createur, parId.values()));
        c.setClasseId(classeId);
        c.setCreatedBy(createur.getId());
        c.setCreatedAt(maintenant());
        c.setParticipantsKey(participantsKey(parId.keySet()));
        c = conversations().saveAndFlush(c);
        for (String uid : parId.keySet()) {
            participants().ajouterSiAbsent(c.getId(), uid, c.getCreatedAt());
        }
        return c;
    }

    private static String titreGroupe(String titre, UtilisateursEntity createur, Collection<UtilisateursEntity> membres) {
        if (titre != null && !titre.isBlank()) {
            String t = titre.trim();
            return t.length() > 255 ? t.substring(0, 255) : t;
        }
        String noms = membres.stream()
                .filter(u -> !u.getId().equals(createur.getId()))
                .map(u -> Objects.toString(u.getPrenom(), Objects.toString(u.getNom(), "")).trim())
                .filter(s -> !s.isEmpty())
                .limit(4)
                .collect(Collectors.joining(", "));
        return noms.isEmpty() ? "Groupe" : (noms.length() > 255 ? noms.substring(0, 255) : noms);
    }

    /**
     * Conversation of a message sent through the legacy (e-mail like) endpoints: DIRECT for two people
     * (or a message to oneself), otherwise the latest GROUPE with exactly the same participants (and the same
     * class for a single-class message, no class otherwise), or a new one titled by the subject (class name).
     */
    public ConversationEntity rattacherEnvoi(UtilisateursEntity expediteur, Collection<UtilisateursEntity> destinataires,
                                      String objet, String classeId) {
        Map<String, UtilisateursEntity> parId = new LinkedHashMap<>();
        parId.put(expediteur.getId(), expediteur);
        destinataires.stream().filter(Objects::nonNull).forEach(u -> parId.putIfAbsent(u.getId(), u));
        if (parId.size() <= 2) {
            String autre = parId.keySet().stream().filter(id -> !id.equals(expediteur.getId())).findFirst()
                    .orElse(expediteur.getId());
            return trouverOuCreerDirect(expediteur.getId(), autre);
        }
        // a class message and an individual message to the same people stay in separate conversations
        String key = participantsKey(parId.keySet());
        List<ConversationEntity> groupes = classeId != null
                ? conversations().findByTypeAndParticipantsKeyAndClasseIdOrderByCreatedAtDesc(ConversationEntity.GROUPE, key, classeId)
                : conversations().findByTypeAndParticipantsKeyAndClasseIdIsNullOrderByCreatedAtDesc(ConversationEntity.GROUPE, key);
        if (!groupes.isEmpty()) return groupes.get(0);
        return creerGroupe(expediteur, parId.values(), objet, classeId);
    }

    /** After a message was saved in the conversation: last_message_at, and the sender has read up to it. */
    public void apresEnvoi(ConversationEntity c, String expediteurId, LocalDateTime date) {
        if (c.getLastMessageAt() == null || c.getLastMessageAt().isBefore(date)) {
            c.setLastMessageAt(date);
            conversations().save(c);
        }
        participants().findById(new ConversationParticipantId(c.getId(), expediteurId)).ifPresent(p -> {
            boolean modifie = p.isMarqueNonLu();
            p.setMarqueNonLu(false);
            if (p.getLastReadAt() == null || p.getLastReadAt().isBefore(date)) {
                p.setLastReadAt(date);
                modifie = true;
            }
            if (modifie) participants().save(p);
        });
    }

    public Set<String> idsMuets(String conversationId) {
        return participantsDe(conversationId).stream().filter(ConversationParticipantEntity::isMuted)
                .map(p -> p.getId().getUtilisateurId()).collect(Collectors.toSet());
    }

    // ── Lecture ──────────────────────────────────────────────────────────────

    public List<ConversationDto> mesConversations() {
        UtilisateursEntity moi = appelant();
        List<ConversationEntity> convs = conversations().findVisibleFor(moi.getId());
        if (convs.isEmpty()) return new ArrayList<>();
        List<String> ids = convs.stream().map(ConversationEntity::getId).collect(Collectors.toList());

        Map<String, List<ConversationParticipantEntity>> parConv = participants().findByConversationIds(ids).stream()
                .collect(Collectors.groupingBy(p -> p.getId().getConversationId()));

        Map<String, String> dernierParConv = new HashMap<>();
        for (Object[] row : messages().findDerniersMessages(moi.getId(), ids)) {
            dernierParConv.put((String) row[0], (String) row[1]);
        }
        Map<String, MessagesEntity> derniers = messages().findAllById(dernierParConv.values()).stream()
                .collect(Collectors.toMap(MessagesEntity::getId, Function.identity()));
        Contexte ctx = contexte(moi.getId(), ids);
        return convs.stream()
                .map(c -> toDto(c, moi.getId(), parConv.getOrDefault(c.getId(), List.of()),
                        derniers.get(dernierParConv.get(c.getId())), ctx))
                .sorted(Comparator.comparing((ConversationDto d) ->
                        Objects.toString(d.getLastMessageAt() != null ? d.getLastMessageAt() : d.getCreatedAt(), "")).reversed())
                .collect(Collectors.toList());
    }

    public ConversationDto obtenir(String conversationId) {
        UtilisateursEntity moi = appelant();
        return toDto(verifierParticipant(conversationId, moi.getId()), moi.getId());
    }

    public ConversationDto parMessage(String messageId) {
        UtilisateursEntity moi = appelant();
        MessagesEntity m = messages().findById(messageId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Message introuvable"));
        if (m.getConversationId() == null) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Ce message n'appartient à aucune conversation");
        }
        return toDto(verifierParticipant(m.getConversationId(), moi.getId()), moi.getId());
    }

    /**
     * total = unread messages, plus 1 for each visible conversation marked as unread without unread messages;
     * conversations = conversations matching the "Non lus" filter.
     */
    public Map<String, Long> compterNonLus() {
        UtilisateursEntity moi = appelant();
        Map<String, Long> parConv = nonLusParConversation(moi.getId());
        long total = parConv.values().stream().mapToLong(Long::longValue).sum();
        long conversationsNonLues = parConv.values().stream().filter(n -> n > 0).count();
        List<ConversationParticipantEntity> marques = participants().findMarquesNonLus(moi.getId());
        if (!marques.isEmpty()) {
            Set<String> visibles = conversations().findVisibleFor(moi.getId()).stream()
                    .map(ConversationEntity::getId).collect(Collectors.toSet());
            long enPlus = marques.stream().map(p -> p.getId().getConversationId())
                    .filter(visibles::contains)
                    .filter(cid -> parConv.getOrDefault(cid, 0L) == 0L)
                    .count();
            total += enPlus;
            conversationsNonLues += enPlus;
        }
        Map<String, Long> r = new LinkedHashMap<>();
        r.put("total", total);
        r.put("conversations", conversationsNonLues);
        return r;
    }

    private Map<String, Long> nonLusParConversation(String uid) {
        Map<String, Long> r = new HashMap<>();
        for (Object[] row : messages().countNonLusParConversation(uid)) {
            r.put((String) row[0], ((Number) row[1]).longValue());
        }
        return r;
    }

    /** Per-viewer data of a batch of conversations (unread, starred messages, class origin, user types). */
    private record Contexte(Map<String, Long> nonLus, Map<String, Long> favoris, Map<String, Long> classes,
                            Map<String, String> types) {}

    private Contexte contexte(String viewerId, Collection<String> ids) {
        return new Contexte(nonLusParConversation(viewerId),
                compteParConversation(messages().countFavorisParConversation(viewerId, ids)),
                compteParConversation(conversations().countClassesParConversation(ids)),
                new HashMap<>());
    }

    private static Map<String, Long> compteParConversation(List<Object[]> rows) {
        Map<String, Long> r = new HashMap<>();
        for (Object[] row : rows) r.put((String) row[0], ((Number) row[1]).longValue());
        return r;
    }

    /**
     * Filter "Général": CLASSE (class conversation, or started by a message to one class), GENERAL (started by a
     * message to several classes), GROUPE (other groups); null for an individual conversation.
     */
    static String categorie(String type, String classeId, long classesPremierMessage) {
        if (classeId != null || classesPremierMessage == 1) return "CLASSE";
        if (classesPremierMessage > 1) return "GENERAL";
        if (ConversationEntity.GROUPE.equals(type)) return "GROUPE";
        return null;
    }

    public ConversationDto toDto(ConversationEntity c, String viewerId) {
        Map<String, String> dernier = new HashMap<>();
        for (Object[] row : messages().findDerniersMessages(viewerId, List.of(c.getId()))) {
            dernier.put((String) row[0], (String) row[1]);
        }
        MessagesEntity m = dernier.isEmpty() ? null : messages().findById(dernier.get(c.getId())).orElse(null);
        return toDto(c, viewerId, participants().findByConversationIds(List.of(c.getId())), m,
                contexte(viewerId, List.of(c.getId())));
    }

    private ConversationDto toDto(ConversationEntity c, String viewerId, List<ConversationParticipantEntity> parts,
                                  MessagesEntity dernier, Contexte ctx) {
        long nonLus = ctx.nonLus().getOrDefault(c.getId(), 0L);
        Map<String, String> types = ctx.types();
        ConversationDto dto = new ConversationDto();
        dto.setId(c.getId());
        dto.setType(c.getType());
        dto.setTitre(c.getTitre());
        dto.setClasseId(c.getClasseId());
        dto.setCreatedBy(c.getCreatedBy());
        dto.setCreatedAt(ServerDateTimes.format(c.getCreatedAt()));
        dto.setLastMessageAt(ServerDateTimes.format(c.getLastMessageAt()));
        dto.setNonLus(nonLus);
        dto.setContientFavori(ctx.favoris().getOrDefault(c.getId(), 0L) > 0);
        dto.setCategorie(categorie(c.getType(), c.getClasseId(), ctx.classes().getOrDefault(c.getId(), 0L)));
        dto.setGeneral(dto.getCategorie() != null);
        for (ConversationParticipantEntity p : parts) {
            UtilisateursEntity u = p.getUtilisateur();
            ConversationDto.Participant pd = new ConversationDto.Participant();
            pd.setId(p.getId().getUtilisateurId());
            if (u != null) {
                pd.setPrenom(u.getPrenom());
                pd.setNom(u.getNom());
                pd.setTypeUtilisateur(types.computeIfAbsent(u.getId(), id -> typeUtilisateur(u)));
            }
            pd.setLastReadAt(ServerDateTimes.format(p.getLastReadAt()));
            dto.getParticipants().add(pd);
            if (pd.getId().equals(viewerId)) {
                dto.setMuted(p.isMuted());
                dto.setFavori(p.isFavori());
                dto.setMarqueNonLu(p.isMarqueNonLu());
            }
        }
        dto.getParticipants().sort(Comparator.comparing((ConversationDto.Participant p) -> Objects.toString(p.getNom(), "").toLowerCase())
                .thenComparing(p -> Objects.toString(p.getPrenom(), "").toLowerCase()));
        if (dernier != null) {
            ConversationDto.DernierMessage dm = new ConversationDto.DernierMessage();
            dm.setId(dernier.getId());
            dm.setSupprime(dernier.isDeleted());
            dm.setContenu(dernier.isDeleted() ? "" : apercu(dernier));
            if (dernier.getExpediteurEntity() != null) {
                dm.setExpediteurId(dernier.getExpediteurEntity().getId());
                dm.setExpediteurPrenom(dernier.getExpediteurEntity().getPrenom());
                dm.setExpediteurNom(dernier.getExpediteurEntity().getNom());
            }
            dm.setDate(dateDe(dernier));
            dto.setDernierMessage(dm);
        }
        return dto;
    }

    private String typeUtilisateur(UtilisateursEntity u) {
        if (Boolean.TRUE.equals(u.getAdmin())) return "ADMIN";
        String type = userSubtypeService.typeUtilisateur(u.getId());
        return type == null || "GESTIONNAIRE".equals(type) ? "UTILISATEUR" : type;
    }

    // ── Messages d'une conversation ──────────────────────────────────────────

    public ChatMessagePageDto messagesDe(String conversationId, String before, int size) {
        UtilisateursEntity moi = appelant();
        verifierParticipant(conversationId, moi.getId());
        ConversationParticipantEntity moiP = participant(conversationId, moi.getId());
        LocalDateTime plancher = moiP.plancherHistorique();
        LocalDateTime apres = plancher != null ? plancher : ORIGINE;
        int taille = Math.max(1, Math.min(size <= 0 ? 30 : size, 100));
        PageRequest page = PageRequest.of(0, taille + 1);

        List<MessagesEntity> lot;
        if (before == null || before.isBlank()) {
            lot = messages().findPageConversation(conversationId, moi.getId(), apres, page);
        } else {
            LocalDateTime avant;
            String avantId;
            Optional<MessagesEntity> curseur = messages().findById(before.trim());
            if (curseur.isPresent() && conversationId.equals(curseur.get().getConversationId())
                    && curseur.get().getDateEnvoi() != null) {
                avant = curseur.get().getDateEnvoi();
                avantId = curseur.get().getId();
            } else {
                try {
                    avant = ServerDateTimes.parseUtcLocalDateTime(before.trim());
                } catch (Exception e) {
                    avant = null;
                }
                if (avant == null) {
                    throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                            "Paramètre before invalide : identifiant de message de la conversation ou date ISO attendu");
                }
                avantId = "";
            }
            lot = messages().findPageConversationAvant(conversationId, moi.getId(), apres, avant, avantId, page);
        }
        boolean hasMore = lot.size() > taille;
        List<MessagesEntity> retenus = new ArrayList<>(hasMore ? lot.subList(0, taille) : lot);
        Collections.reverse(retenus);

        List<ConversationParticipantEntity> parts = participantsDe(conversationId);
        List<MessageStatutEntity> mesStatuts = statuts().findAllById(retenus.stream()
                .map(m -> new MessageStatutId(moi.getId(), m.getId())).collect(Collectors.toList()));
        Set<String> lusParMoi = mesStatuts.stream().filter(MessageStatutEntity::isLu)
                .map(s -> s.getId().getMessageId()).collect(Collectors.toSet());
        Set<String> favorisDeMoi = mesStatuts.stream().filter(MessageStatutEntity::isFavori)
                .map(s -> s.getId().getMessageId()).collect(Collectors.toSet());
        List<ChatMessageDto> dtos = retenus.stream()
                .map(m -> {
                    ChatMessageDto d = toChat(m, moi.getId(), parts, lusParMoi.contains(m.getId()));
                    d.setFavori(favorisDeMoi.contains(m.getId()));
                    return d;
                })
                .collect(Collectors.toList());
        return new ChatMessagePageDto(dtos, hasMore);
    }

    /** Chat view of a message for {@code viewerId} (statut/lu computed from participants' last_read_at). */
    public ChatMessageDto toChat(MessagesEntity m, String viewerId, List<ConversationParticipantEntity> parts, boolean marqueLu) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(m.getId());
        dto.setConversationId(m.getConversationId());
        String expId = m.getExpediteurEntity() != null ? m.getExpediteurEntity().getId() : null;
        if (m.getExpediteurEntity() != null) {
            dto.setExpediteurId(expId);
            dto.setExpediteurNom(m.getExpediteurEntity().getNom());
            dto.setExpediteurPrenom(m.getExpediteurEntity().getPrenom());
        }
        dto.setDate(dateDe(m));
        dto.setSupprimePourTous(m.isDeleted());
        if (m.isDeleted()) {
            dto.setContenu("");
            dto.setMedias(new ArrayList<>());
        } else {
            dto.setContenu(Objects.toString(m.getContenu(), ""));
            dto.setMedias(mapMedias(m));
        }
        MessagesEntity r = m.getReplyToEntity();
        if (r != null) {
            ChatMessageDto.ReplyTo rt = new ChatMessageDto.ReplyTo();
            rt.setId(r.getId());
            if (r.getExpediteurEntity() != null) {
                rt.setExpediteurId(r.getExpediteurEntity().getId());
                rt.setExpediteurNom(r.getExpediteurEntity().getNom());
                rt.setExpediteurPrenom(r.getExpediteurEntity().getPrenom());
            }
            rt.setSupprime(r.isDeleted());
            rt.setExtrait(r.isDeleted() ? "" : extrait(r));
            dto.setReplyTo(rt);
        }
        LocalDateTime date = m.getDateEnvoi();
        if (viewerId != null && viewerId.equals(expId)) {
            List<ConversationParticipantEntity> autres = parts.stream()
                    .filter(p -> !p.getId().getUtilisateurId().equals(viewerId)).collect(Collectors.toList());
            boolean luParTous = !autres.isEmpty() && date != null && autres.stream()
                    .allMatch(p -> p.getLastReadAt() != null && !p.getLastReadAt().isBefore(date));
            dto.setStatut(luParTous ? "READ" : "SENT");
            dto.setLu(luParTous);
        } else {
            boolean lu = marqueLu || parts.stream()
                    .filter(p -> p.getId().getUtilisateurId().equals(viewerId))
                    .anyMatch(p -> p.getLastReadAt() != null && date != null && !p.getLastReadAt().isBefore(date));
            dto.setLu(lu);
        }
        return dto;
    }

    // ── Lu / masquer / muet ──────────────────────────────────────────────────

    public Map<String, String> marquerLue(String conversationId) {
        UtilisateursEntity moi = appelant();
        verifierParticipant(conversationId, moi.getId());
        ConversationParticipantEntity p = participant(conversationId, moi.getId());
        LocalDateTime now = maintenant();
        p.setLastReadAt(now);
        p.setMarqueNonLu(false);
        participants().save(p);

        // keep the per-message status (legacy unread counters) consistent
        Date maintenantDate = new Date();
        List<MessageStatutEntity> aMarquer = new ArrayList<>();
        for (MessagesEntity m : messages().findNonMarquesLus(conversationId, moi.getId(), now)) {
            MessageStatutId id = new MessageStatutId(moi.getId(), m.getId());
            MessageStatutEntity s = statuts().findById(id).orElseGet(() -> {
                MessageStatutEntity n = new MessageStatutEntity();
                n.setId(id);
                n.setUtilisateur(moi);
                n.setMessage(m);
                return n;
            });
            s.setLu(true);
            s.setDateLecture(maintenantDate);
            aMarquer.add(s);
        }
        if (!aMarquer.isEmpty()) statuts().saveAll(aMarquer);

        String iso = ServerDateTimes.format(now);
        List<String> ids = participantsDe(conversationId).stream().map(x -> x.getId().getUtilisateurId()).collect(Collectors.toList());
        try {
            messagePublisher.pushConversationRead(conversationId, moi.getId(), iso, ids);
        } catch (Exception e) {
            log.warn("Failed to push read receipt: {}", e.getMessage());
        }
        Map<String, String> r = new LinkedHashMap<>();
        r.put("conversationId", conversationId);
        r.put("lastReadAt", iso);
        return r;
    }

    /** "Supprimer la discussion": moves it to the caller's trash (restorable, reappears on a new message). */
    public void masquer(String conversationId) {
        UtilisateursEntity moi = appelant();
        verifierParticipant(conversationId, moi.getId());
        ConversationParticipantEntity p = participant(conversationId, moi.getId());
        LocalDateTime now = maintenant();
        p.setDeletedAt(now);
        p.setLastReadAt(now);
        p.setMarqueNonLu(false);
        participants().save(p);
        notifierMoi(conversationId, moi.getId());
    }

    public ConversationDto definirMuet(String conversationId, boolean muted) {
        UtilisateursEntity moi = appelant();
        ConversationEntity c = verifierParticipant(conversationId, moi.getId());
        ConversationParticipantEntity p = participant(conversationId, moi.getId());
        p.setMuted(muted);
        participants().save(p);
        notifierMoi(conversationId, moi.getId());
        return toDto(c, moi.getId());
    }

    /** Star / unstar the conversation for the caller only. */
    public ConversationDto definirFavori(String conversationId, boolean favori) {
        UtilisateursEntity moi = appelant();
        ConversationEntity c = verifierParticipant(conversationId, moi.getId());
        ConversationParticipantEntity p = participant(conversationId, moi.getId());
        p.setFavori(favori);
        participants().save(p);
        notifierMoi(conversationId, moi.getId());
        return toDto(c, moi.getId());
    }

    /** "Marquer comme non lu" (caller only); cleared when the caller reads or writes in the conversation. */
    public ConversationDto marquerNonLue(String conversationId) {
        UtilisateursEntity moi = appelant();
        ConversationEntity c = verifierParticipant(conversationId, moi.getId());
        ConversationParticipantEntity p = participant(conversationId, moi.getId());
        p.setMarqueNonLu(true);
        participants().save(p);
        notifierMoi(conversationId, moi.getId());
        return toDto(c, moi.getId());
    }

    // ── Corbeille (discussions) ──────────────────────────────────────────────

    /** Conversations in the caller's trash, most recently deleted first, with their last message before deletion. */
    public List<ConversationDto> conversationsCorbeille(String uid) {
        List<ConversationParticipantEntity> entrees = participants().findCorbeille(uid);
        if (entrees.isEmpty()) return new ArrayList<>();
        Map<String, LocalDateTime> supprimeLe = entrees.stream().collect(Collectors.toMap(
                p -> p.getId().getConversationId(), ConversationParticipantEntity::getDeletedAt, (a, b) -> a));
        List<String> ids = new ArrayList<>(supprimeLe.keySet());
        Map<String, List<ConversationParticipantEntity>> parConv = participants().findByConversationIds(ids).stream()
                .collect(Collectors.groupingBy(p -> p.getId().getConversationId()));
        Map<String, String> dernierParConv = new HashMap<>();
        for (Object[] row : messages().findDerniersMessagesCorbeille(uid, ids)) {
            dernierParConv.put((String) row[0], (String) row[1]);
        }
        Map<String, MessagesEntity> derniers = messages().findAllById(dernierParConv.values()).stream()
                .collect(Collectors.toMap(MessagesEntity::getId, Function.identity()));
        Contexte ctx = new Contexte(new HashMap<>(), new HashMap<>(),
                compteParConversation(conversations().countClassesParConversation(ids)), new HashMap<>());
        return conversations().findAllById(ids).stream()
                .map(c -> {
                    ConversationDto d = toDto(c, uid, parConv.getOrDefault(c.getId(), List.of()),
                            derniers.get(dernierParConv.get(c.getId())), ctx);
                    LocalDateTime le = supprimeLe.get(c.getId());
                    d.setSupprimeLe(ServerDateTimes.format(le));
                    d.setReapparue(c.getLastMessageAt() != null && le != null && c.getLastMessageAt().isAfter(le));
                    d.setMarqueNonLu(false);
                    return d;
                })
                .sorted(Comparator.comparing((ConversationDto d) -> Objects.toString(d.getSupprimeLe(), "")).reversed())
                .collect(Collectors.toList());
    }

    /** Restores a conversation of the caller's trash: it is listed again with its whole history. */
    public ConversationDto restaurer(String conversationId) {
        UtilisateursEntity moi = appelant();
        ConversationEntity c = verifierParticipant(conversationId, moi.getId());
        ConversationParticipantEntity p = participant(conversationId, moi.getId());
        if (p.getDeletedAt() == null) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Cette discussion n'est pas dans votre corbeille");
        }
        p.setDeletedAt(null);
        participants().save(p);
        notifierMoi(conversationId, moi.getId());
        return toDto(c, moi.getId());
    }

    /**
     * Permanently deletes a conversation of the trash for {@code uid}: its history up to the deletion is never
     * shown again (the conversation comes back, empty, with the next message).
     *
     * @return the new history floor
     */
    public LocalDateTime supprimerDefinitivement(String conversationId, String uid) {
        verifierParticipant(conversationId, uid);
        ConversationParticipantEntity p = participant(conversationId, uid);
        if (p.getDeletedAt() == null) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Cette discussion n'est pas dans votre corbeille");
        }
        LocalDateTime plancher = p.plancherHistorique();
        p.setHistoriqueDepuis(plancher);
        p.setDeletedAt(null);
        p.setMarqueNonLu(false);
        p.setFavori(false); // comes back as a fresh conversation with the next message
        participants().save(p);
        notifierMoi(conversationId, uid);
        return plancher;
    }

    public List<String> idsCorbeille(String uid) {
        return participants().findCorbeille(uid).stream().map(p -> p.getId().getConversationId()).collect(Collectors.toList());
    }

    /** History floor of the user in the conversation (null: everything visible). */
    public LocalDateTime plancher(String conversationId, String uid) {
        return participants().findById(new ConversationParticipantId(conversationId, uid))
                .map(ConversationParticipantEntity::plancherHistorique).orElse(null);
    }

    /** Display title of a conversation for a viewer: group title, or the other participant's name. */
    public String titrePour(ConversationEntity c, String viewerId) {
        if (ConversationEntity.GROUPE.equals(c.getType()) && c.getTitre() != null && !c.getTitre().isBlank()) {
            return c.getTitre();
        }
        String noms = participantsDe(c.getId()).stream()
                .filter(p -> !p.getId().getUtilisateurId().equals(viewerId))
                .map(ConversationParticipantEntity::getUtilisateur)
                .filter(Objects::nonNull)
                .map(u -> (Objects.toString(u.getPrenom(), "") + " " + Objects.toString(u.getNom(), "")).trim())
                .filter(n -> !n.isEmpty())
                .limit(4)
                .collect(Collectors.joining(", "));
        if (!noms.isEmpty()) return noms;
        return c.getTitre() != null && !c.getTitre().isBlank() ? c.getTitre() : "Moi";
    }

    private void notifierMoi(String conversationId, String uid) {
        try {
            messagePublisher.pushConversationUpdated(conversationId, uid);
        } catch (Exception e) {
            log.warn("Failed to push conversation update: {}", e.getMessage());
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    static LocalDateTime maintenant() {
        return ServerDateTimes.nowUtc().truncatedTo(ChronoUnit.MILLIS);
    }

    private static String dateDe(MessagesEntity m) {
        return m.getDateEnvoi() != null ? ServerDateTimes.format(m.getDateEnvoi()) : ServerDateTimes.normalize(m.getDateCreation());
    }

    /** Preview for the conversation list: text, or the attachment label. */
    static String apercu(MessagesEntity m) {
        String t = Objects.toString(m.getContenu(), "").trim();
        if (!t.isEmpty()) return t.length() > 200 ? t.substring(0, 200) : t;
        return m.getPiecesJointesEntities() != null && !m.getPiecesJointesEntities().isEmpty() ? PIECE_JOINTE : "";
    }

    static String extrait(MessagesEntity m) {
        String t = Objects.toString(m.getContenu(), "").trim();
        if (!t.isEmpty()) return t.length() > EXTRAIT_MAX ? t.substring(0, EXTRAIT_MAX) + "…" : t;
        return m.getPiecesJointesEntities() != null && !m.getPiecesJointesEntities().isEmpty() ? PIECE_JOINTE : "";
    }

    public List<Media> mapMedias(MessagesEntity entity) {
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

    private ConversationRepository conversations() {
        return daoAccessorService.getRepository(ConversationRepository.class);
    }

    private ConversationParticipantRepository participants() {
        return daoAccessorService.getRepository(ConversationParticipantRepository.class);
    }

    private MessagesRepository messages() {
        return daoAccessorService.getRepository(MessagesRepository.class);
    }

    private MessageStatutRepository statuts() {
        return daoAccessorService.getRepository(MessageStatutRepository.class);
    }

    private UtilisateursRepository users() {
        return daoAccessorService.getRepository(UtilisateursRepository.class);
    }
}
