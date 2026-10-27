package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.security.UserSubtypeService;
import cmr.notep.business.services.MediaService;
import cmr.notep.business.services.MessagePublisher;
import cmr.notep.business.services.NotificationService;
import cmr.notep.interfaces.dto.ConversationDto;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.*;
import cmr.notep.ressourcesjpa.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Filtres de la liste des discussions (favori, non lu, général) et corbeille de la messagerie :
 * tout est propre à l'appelant.
 */
class ConversationsFiltresCorbeilleTest {

    private static final String MOI = "u-moi";
    private static final String AUTRE = "u-autre";
    private static final String CID = "c-1";

    private DaoAccessorService dao;
    private ConversationRepository convRepo;
    private ConversationParticipantRepository partRepo;
    private MessagesRepository msgRepo;
    private MessageStatutRepository statutRepo;
    private UtilisateursRepository userRepo;
    private MessagePublisher publisher;
    private ConversationsBusiness business;

    private UtilisateursEntity moi;
    private UtilisateursEntity autre;
    private ConversationEntity conv;
    private ConversationParticipantEntity moiP;
    private ConversationParticipantEntity autreP;

    @BeforeEach
    void setUp() {
        dao = mock(DaoAccessorService.class);
        convRepo = mock(ConversationRepository.class);
        partRepo = mock(ConversationParticipantRepository.class);
        msgRepo = mock(MessagesRepository.class);
        statutRepo = mock(MessageStatutRepository.class);
        userRepo = mock(UtilisateursRepository.class);
        publisher = mock(MessagePublisher.class);
        when(dao.getRepository(ConversationRepository.class)).thenReturn(convRepo);
        when(dao.getRepository(ConversationParticipantRepository.class)).thenReturn(partRepo);
        when(dao.getRepository(MessagesRepository.class)).thenReturn(msgRepo);
        when(dao.getRepository(MessageStatutRepository.class)).thenReturn(statutRepo);
        when(dao.getRepository(UtilisateursRepository.class)).thenReturn(userRepo);
        business = new ConversationsBusiness(dao, mock(UserSubtypeService.class), mock(MediaService.class), publisher);

        moi = user(MOI, "moi@x.cm", "Awa", "Moi");
        autre = user(AUTRE, "autre@x.cm", "Paul", "Autre");
        when(userRepo.findByEmail("moi@x.cm")).thenReturn(Optional.of(moi));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "moi@x.cm", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        conv = new ConversationEntity();
        conv.setId(CID);
        conv.setType(ConversationEntity.DIRECT);
        conv.setCreatedBy(AUTRE);
        conv.setCreatedAt(LocalDateTime.of(2026, 10, 1, 8, 0));
        conv.setLastMessageAt(LocalDateTime.of(2026, 10, 2, 8, 0));
        moiP = participant(CID, moi);
        autreP = participant(CID, autre);
        when(convRepo.findById(CID)).thenReturn(Optional.of(conv));
        when(convRepo.findAllById(any())).thenReturn(List.of(conv));
        when(partRepo.findById(new ConversationParticipantId(CID, MOI))).thenReturn(Optional.of(moiP));
        when(partRepo.findById(new ConversationParticipantId(CID, AUTRE))).thenReturn(Optional.of(autreP));
        when(partRepo.findByConversationIds(any())).thenReturn(List.of(moiP, autreP));
        when(partRepo.findByIdConversationId(CID)).thenReturn(List.of(moiP, autreP));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private static UtilisateursEntity user(String id, String email, String prenom, String nom) {
        UtilisateursEntity u = new UtilisateursEntity();
        u.setId(id);
        u.setEmail(email);
        u.setPrenom(prenom);
        u.setNom(nom);
        return u;
    }

    private static ConversationParticipantEntity participant(String cid, UtilisateursEntity u) {
        ConversationParticipantEntity p = new ConversationParticipantEntity();
        p.setId(new ConversationParticipantId(cid, u.getId()));
        p.setUtilisateur(u);
        p.setJoinedAt(LocalDateTime.of(2026, 10, 1, 8, 0));
        return p;
    }

    private static void assertCode(SchoolErrorCode code, Runnable r) {
        SchoolException e = assertThrows(SchoolException.class, r::run);
        assertEquals(code, e.getCode());
    }

    // ── Général ─────────────────────────────────────────────────────────────

    @Test
    void categorieGeneral() {
        assertNull(ConversationsBusiness.categorie("DIRECT", null, 0));
        assertEquals("CLASSE", ConversationsBusiness.categorie("GROUPE", "classe-1", 0));
        assertEquals("CLASSE", ConversationsBusiness.categorie("DIRECT", null, 1));
        assertEquals("GENERAL", ConversationsBusiness.categorie("GROUPE", null, 3));
        assertEquals("GROUPE", ConversationsBusiness.categorie("GROUPE", null, 0));
    }

    @Test
    void dtoPorteLesDonneesDesFiltres() {
        conv.setType(ConversationEntity.GROUPE);
        moiP.setFavori(true);
        moiP.setMarqueNonLu(true);
        autreP.setFavori(false); // the other participant's flags never leak
        when(convRepo.countClassesParConversation(any())).thenReturn(List.<Object[]>of(new Object[]{CID, 2L}));
        when(msgRepo.countFavorisParConversation(eq(MOI), any())).thenReturn(List.<Object[]>of(new Object[]{CID, 1L}));
        when(msgRepo.countNonLusParConversation(MOI)).thenReturn(List.<Object[]>of(new Object[]{CID, 4L}));

        ConversationDto dto = business.obtenir(CID);

        assertTrue(dto.isFavori());
        assertTrue(dto.isContientFavori());
        assertTrue(dto.isMarqueNonLu());
        assertEquals(4L, dto.getNonLus());
        assertEquals("GROUPE", dto.getType());
        assertEquals("GENERAL", dto.getCategorie());
        assertTrue(dto.isGeneral());
        assertNull(dto.getSupprimeLe());
    }

    @Test
    void favoriDuPointDeVueDeLAutreParticipant() {
        autreP.setFavori(true);
        ConversationDto dto = business.obtenir(CID);
        assertFalse(dto.isFavori());
        assertFalse(dto.isGeneral());
    }

    // ── Favori / non lu ─────────────────────────────────────────────────────

    @Test
    void favoriPropreAuParticipantEtDiffuseAMoiSeulement() {
        ConversationDto dto = business.definirFavori(CID, true);

        assertTrue(moiP.isFavori());
        assertFalse(autreP.isFavori());
        assertTrue(dto.isFavori());
        verify(partRepo).save(moiP);
        verify(publisher).pushConversationUpdated(CID, MOI);
        verify(publisher, never()).pushConversationUpdated(CID, AUTRE);

        business.definirFavori(CID, false);
        assertFalse(moiP.isFavori());
    }

    @Test
    void nonParticipantNePeutNiMettreEnFavoriNiRestaurer() {
        when(partRepo.findById(new ConversationParticipantId(CID, MOI))).thenReturn(Optional.empty());
        assertCode(SchoolErrorCode.OPERATION_INTERDITE, () -> business.definirFavori(CID, true));
        assertCode(SchoolErrorCode.OPERATION_INTERDITE, () -> business.marquerNonLue(CID));
        assertCode(SchoolErrorCode.OPERATION_INTERDITE, () -> business.restaurer(CID));
        assertCode(SchoolErrorCode.OPERATION_INTERDITE, () -> business.supprimerDefinitivement(CID, MOI));
        verify(partRepo, never()).save(any());
    }

    @Test
    void marquerNonLuPuisLireEfface() {
        business.marquerNonLue(CID);
        assertTrue(moiP.isMarqueNonLu());
        verify(publisher).pushConversationUpdated(CID, MOI);

        business.marquerLue(CID);
        assertFalse(moiP.isMarqueNonLu());
        assertNotNull(moiP.getLastReadAt());
    }

    @Test
    void ecrireDansLaDiscussionEffaceLeMarquageNonLu() {
        moiP.setMarqueNonLu(true);
        business.apresEnvoi(conv, MOI, LocalDateTime.of(2026, 10, 3, 9, 0));
        assertFalse(moiP.isMarqueNonLu());
    }

    @Test
    void compteurNonLusInclutLesDiscussionsMarquees() {
        ConversationEntity c2 = new ConversationEntity();
        c2.setId("c-2");
        ConversationParticipantEntity marque = participant("c-2", moi);
        marque.setMarqueNonLu(true);
        when(msgRepo.countNonLusParConversation(MOI)).thenReturn(List.<Object[]>of(new Object[]{CID, 3L}));
        when(partRepo.findMarquesNonLus(MOI)).thenReturn(List.of(marque));
        when(convRepo.findVisibleFor(MOI)).thenReturn(List.of(conv, c2));

        Map<String, Long> r = business.compterNonLus();

        assertEquals(4L, r.get("total"));
        assertEquals(2L, r.get("conversations"));
    }

    @Test
    void discussionMarqueeMasqueeNeCompteParmiLesNonLus() {
        ConversationParticipantEntity marque = participant("c-cachee", moi);
        marque.setMarqueNonLu(true);
        when(partRepo.findMarquesNonLus(MOI)).thenReturn(List.of(marque));
        when(convRepo.findVisibleFor(MOI)).thenReturn(List.of(conv));
        assertEquals(0L, business.compterNonLus().get("total"));
    }

    // ── Corbeille (discussions) ─────────────────────────────────────────────

    @Test
    void supprimerMetDansLaCorbeillePuisRestaurerRendLHistorique() {
        business.masquer(CID);
        LocalDateTime supprimee = moiP.getDeletedAt();
        assertNotNull(supprimee);
        assertEquals(supprimee, moiP.plancherHistorique());
        assertNull(autreP.getDeletedAt());
        verify(publisher).pushConversationUpdated(CID, MOI);

        business.restaurer(CID);
        assertNull(moiP.getDeletedAt());
        assertNull(moiP.plancherHistorique());
    }

    @Test
    void restaurerUneDiscussionHorsCorbeilleEstRefuse() {
        assertCode(SchoolErrorCode.INVALID_INPUT, () -> business.restaurer(CID));
        assertCode(SchoolErrorCode.INVALID_INPUT, () -> business.supprimerDefinitivement(CID, MOI));
    }

    @Test
    void suppressionDefinitiveFixeLePlancherDHistorique() {
        LocalDateTime t1 = LocalDateTime.of(2026, 10, 5, 10, 0);
        moiP.setDeletedAt(t1);
        moiP.setFavori(true);

        LocalDateTime plancher = business.supprimerDefinitivement(CID, MOI);
        assertFalse(moiP.isFavori());

        assertEquals(t1, plancher);
        assertEquals(t1, moiP.getHistoriqueDepuis());
        assertNull(moiP.getDeletedAt());
        assertEquals(t1, moiP.plancherHistorique());

        // deleted again later: the floor is the later of both dates
        LocalDateTime t2 = LocalDateTime.of(2026, 10, 9, 10, 0);
        moiP.setDeletedAt(t2);
        assertEquals(t2, moiP.plancherHistorique());
        moiP.setDeletedAt(LocalDateTime.of(2026, 10, 1, 0, 0));
        assertEquals(t1, moiP.plancherHistorique());
    }

    @Test
    void corbeilleNeListeQueLesEntreesDeLAppelant() {
        LocalDateTime le = LocalDateTime.of(2026, 10, 1, 12, 0);
        moiP.setDeletedAt(le);
        when(partRepo.findCorbeille(MOI)).thenReturn(List.of(moiP));

        List<ConversationDto> corbeille = business.conversationsCorbeille(MOI);

        verify(partRepo).findCorbeille(MOI);
        verify(partRepo, never()).findCorbeille(AUTRE);
        assertEquals(1, corbeille.size());
        assertEquals(CID, corbeille.get(0).getId());
        assertNotNull(corbeille.get(0).getSupprimeLe());
        // a newer message (2026-10-02) made it reappear: only the older history stays in the trash
        assertTrue(corbeille.get(0).isReapparue());
    }

    @Test
    void corbeilleVide() {
        when(partRepo.findCorbeille(MOI)).thenReturn(List.of());
        assertTrue(business.conversationsCorbeille(MOI).isEmpty());
    }

    // ── Corbeille complète (discussions + messages) ─────────────────────────

    @Test
    void viderLaCorbeilleSupprimeDefinitivementToutPourLAppelant() {
        ConversationsBusiness conversations = mock(ConversationsBusiness.class);
        MessagesBusiness messages = mock(MessagesBusiness.class);
        when(conversations.appelant()).thenReturn(moi);
        when(conversations.idsCorbeille(MOI)).thenReturn(List.of("c-a", "c-b"));
        LocalDateTime pa = LocalDateTime.of(2026, 10, 1, 0, 0);
        when(conversations.supprimerDefinitivement("c-a", MOI)).thenReturn(pa);
        CorbeilleMessagerieBusiness corbeille = new CorbeilleMessagerieBusiness(conversations, messages, publisher);

        corbeille.vider();

        verify(conversations).supprimerDefinitivement("c-a", MOI);
        verify(conversations).supprimerDefinitivement("c-b", MOI);
        verify(messages).purgerCorbeilleConversation(MOI, "c-a", pa);
        verify(messages).viderCorbeille();
        verify(publisher).pushCorbeilleUpdated(MOI, null);
    }

    @Test
    void corbeilleCompteDiscussionsEtMessages() {
        ConversationsBusiness conversations = mock(ConversationsBusiness.class);
        MessagesBusiness messages = mock(MessagesBusiness.class);
        when(conversations.appelant()).thenReturn(moi);
        when(conversations.conversationsCorbeille(MOI)).thenReturn(List.of(new ConversationDto()));
        when(messages.corbeilleChat(MOI)).thenReturn(List.of(new cmr.notep.interfaces.dto.ChatMessageDto(),
                new cmr.notep.interfaces.dto.ChatMessageDto()));

        var dto = new CorbeilleMessagerieBusiness(conversations, messages, publisher).corbeille();

        assertEquals(3L, dto.getTotal());
        verify(messages).corbeilleChat(MOI);
        verify(conversations).conversationsCorbeille(MOI);
    }

    // ── Corbeille (messages) ────────────────────────────────────────────────

    private MessagesBusiness messagesBusiness() {
        return new MessagesBusiness(dao, mock(NotificationService.class), publisher, mock(MediaService.class),
                mock(UserSubtypeService.class), business);
    }

    private MessagesEntity message(String id, UtilisateursEntity expediteur, UtilisateursEntity destinataire) {
        MessagesEntity m = new MessagesEntity();
        m.setId(id);
        m.setExpediteurEntity(expediteur);
        m.setDestinatairesEntities(new ArrayList<>(List.of(destinataire)));
        m.setConversationId(CID);
        m.setContenu("Bonjour");
        m.setDateEnvoi(LocalDateTime.of(2026, 10, 2, 8, 0));
        return m;
    }

    private MessageStatutEntity statut(UtilisateursEntity u, MessagesEntity m) {
        MessageStatutEntity s = new MessageStatutEntity();
        s.setId(new MessageStatutId(u.getId(), m.getId()));
        s.setUtilisateur(u);
        s.setMessage(m);
        return s;
    }

    @Test
    void corbeilleDesMessagesMontreLeContenuEtLaDiscussion() {
        MessagesEntity m = message("m-1", autre, moi);
        MessageStatutEntity s = statut(moi, m);
        s.setSupprime(true);
        s.setFavori(true);
        s.setDateSuppression(new Date());
        when(statutRepo.findByIdUtilisateurId(MOI)).thenReturn(List.of(s));

        var items = messagesBusiness().corbeilleChat(MOI);

        assertEquals(1, items.size());
        assertEquals("Bonjour", items.get(0).getContenu());
        assertEquals(CID, items.get(0).getConversationId());
        assertEquals("Paul Autre", items.get(0).getConversationTitre());
        assertTrue(items.get(0).isFavori());
        assertNotNull(items.get(0).getSupprimeLe());
        verify(statutRepo, never()).findByIdUtilisateurId(AUTRE);
    }

    @Test
    void suppressionDefinitiveDUnMessageHorsDeMaCorbeilleEstRefusee() {
        MessagesEntity m = message("m-2", autre, moi);
        when(msgRepo.findById("m-2")).thenReturn(Optional.of(m));
        when(statutRepo.findByIdUtilisateurId(MOI)).thenReturn(List.of());

        assertCode(SchoolErrorCode.INVALID_INPUT, () -> messagesBusiness().purgerMessage("m-2"));
        verify(statutRepo, never()).save(any());
    }

    @Test
    void suppressionDefinitiveDUnMessageDUneDiscussionDontJeNeSuisPasParticipant() {
        UtilisateursEntity tiers = user("u-tiers", "t@x.cm", "T", "Tiers");
        MessagesEntity m = message("m-3", autre, tiers);
        when(msgRepo.findById("m-3")).thenReturn(Optional.of(m));
        assertCode(SchoolErrorCode.OPERATION_INTERDITE, () -> messagesBusiness().purgerMessage("m-3"));
    }

    @Test
    void suppressionDefinitiveDUnMessageDeMaCorbeille() {
        MessagesEntity m = message("m-4", autre, moi);
        MessageStatutEntity s = statut(moi, m);
        s.setSupprime(true);
        when(msgRepo.findById("m-4")).thenReturn(Optional.of(m));
        when(statutRepo.findByIdUtilisateurId(MOI)).thenReturn(List.of(s));
        when(statutRepo.findById(new MessageStatutId(MOI, "m-4"))).thenReturn(Optional.of(s));
        when(statutRepo.findByIdMessageId("m-4")).thenReturn(List.of(s));

        messagesBusiness().purgerMessage("m-4");

        assertTrue(s.isPurge());
        // the sender still sees it: the row is kept
        verify(msgRepo, never()).delete(any());
        verify(publisher).pushCorbeilleUpdated(MOI, CID);
    }

    @Test
    void purgerLaDiscussionPurgeSesMessagesDeLaCorbeilleSousLePlancher() {
        MessagesEntity avant = message("m-avant", autre, moi);
        avant.setDateEnvoi(LocalDateTime.of(2026, 10, 1, 8, 0));
        MessagesEntity apres = message("m-apres", autre, moi);
        apres.setDateEnvoi(LocalDateTime.of(2026, 10, 9, 8, 0));
        MessageStatutEntity sAvant = statut(moi, avant);
        sAvant.setSupprime(true);
        MessageStatutEntity sApres = statut(moi, apres);
        sApres.setSupprime(true);
        when(statutRepo.findByIdUtilisateurId(MOI)).thenReturn(List.of(sAvant, sApres));
        when(statutRepo.findById(new MessageStatutId(MOI, "m-avant"))).thenReturn(Optional.of(sAvant));
        when(statutRepo.findByIdMessageId(any())).thenReturn(List.of());

        messagesBusiness().purgerCorbeilleConversation(MOI, CID, LocalDateTime.of(2026, 10, 5, 0, 0));

        assertTrue(sAvant.isPurge());
        assertFalse(sApres.isPurge());
    }

    @Test
    void favoriDUnMessageDiffuseAMoi() {
        MessagesEntity m = message("m-5", autre, moi);
        when(msgRepo.findById("m-5")).thenReturn(Optional.of(m));
        when(userRepo.findById(MOI)).thenReturn(Optional.of(moi));

        messagesBusiness().marquerFavori(MOI, "m-5", true);

        verify(statutRepo).save(argThat(s -> s.isFavori() && s.getId().getUtilisateurId().equals(MOI)));
        verify(publisher).pushConversationUpdated(CID, MOI);
    }

    @Test
    void favoriDUnMessagePourUnAutreEstRefuse() {
        assertCode(SchoolErrorCode.OPERATION_INTERDITE, () -> messagesBusiness().marquerFavori(AUTRE, "m-5", true));
    }
}
