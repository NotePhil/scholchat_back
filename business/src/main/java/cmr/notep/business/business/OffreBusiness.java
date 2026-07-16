package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Offre;
import cmr.notep.modele.TypeCibleOffre;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.OffreEntity;
import cmr.notep.ressourcesjpa.repository.OffreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class OffreBusiness {
    private final DaoAccessorService daoAccessorService;

    public List<Offre> listerOffres(TypeCibleOffre cible, boolean toutes) {
        OffreRepository repo = daoAccessorService.getRepository(OffreRepository.class);
        List<OffreEntity> entities;
        if (cible != null) {
            entities = toutes ? repo.findByCible(cible) : repo.findByCibleAndActifTrue(cible);
        } else {
            entities = repo.findAll();
            if (!toutes) {
                entities = entities.stream().filter(OffreEntity::isActif).collect(Collectors.toList());
            }
        }
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }

    public Offre obtenirOffreParId(String id) {
        return toDto(obtenirEntiteParId(id));
    }

    public OffreEntity obtenirEntiteParId(String id) {
        return daoAccessorService.getRepository(OffreRepository.class)
                .findById(id)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.OFFRE_INTROUVABLE, "Offre introuvable avec l'ID: " + id));
    }

    public Offre creerOffre(Offre offre) {
        log.info("Création d'une nouvelle offre: {}", offre.getNom());
        OffreEntity entity = new OffreEntity();
        entity.setId(UUID.randomUUID().toString());
        applyFields(entity, offre);
        entity.setDateCreation(LocalDateTime.now());
        entity.setDateMaj(LocalDateTime.now());
        OffreEntity saved = daoAccessorService.getRepository(OffreRepository.class).save(entity);
        return toDto(saved);
    }

    public Offre modifierOffre(String id, Offre offre) {
        OffreEntity entity = obtenirEntiteParId(id);
        applyFields(entity, offre);
        entity.setDateMaj(LocalDateTime.now());
        OffreEntity saved = daoAccessorService.getRepository(OffreRepository.class).save(entity);
        log.info("Offre modifiée avec succès: {}", id);
        return toDto(saved);
    }

    public void desactiverOffre(String id) {
        OffreEntity entity = obtenirEntiteParId(id);
        entity.setActif(false);
        entity.setDateMaj(LocalDateTime.now());
        daoAccessorService.getRepository(OffreRepository.class).save(entity);
        log.info("Offre désactivée: {}", id);
    }

    private void applyFields(OffreEntity entity, Offre offre) {
        if (offre.getNom() != null) entity.setNom(offre.getNom());
        entity.setDescription(offre.getDescription());
        if (offre.getCible() != null) entity.setCible(offre.getCible());
        entity.setPrixMensuel(offre.getPrixMensuel());
        entity.setDureeMensuelleMinutes(offre.getDureeMensuelleMinutes());
        entity.setPrixAnnuel(offre.getPrixAnnuel());
        entity.setDureeAnnuelleMinutes(offre.getDureeAnnuelleMinutes());
        entity.setNombreClassesInclues(offre.getNombreClassesInclues());
        entity.setClassesBonus(offre.getClassesBonus());
        entity.setEstTest(offre.isEstTest());
        entity.setActif(offre.isActif() || entity.getDateCreation() == null);
        if (offre.getCreatedBy() != null) entity.setCreatedBy(offre.getCreatedBy());
        entity.setDelaiRappelSuppressionMinutes(offre.getDelaiRappelSuppressionMinutes());
        entity.setDelaiSuppressionMinutes(offre.getDelaiSuppressionMinutes());
        entity.setElevesMax(offre.getElevesMax());
        entity.setStockageMaxGo(offre.getStockageMaxGo());
        entity.setMessagerieIncluse(offre.getMessagerieIncluse());
    }

    private Offre toDto(OffreEntity entity) {
        Double reduction = null;
        if (entity.getPrixMensuel() != null && entity.getPrixAnnuel() != null
                && entity.getDureeMensuelleMinutes() != null && entity.getDureeAnnuelleMinutes() != null
                && entity.getDureeMensuelleMinutes() > 0) {
            double moisEquivalents = entity.getDureeAnnuelleMinutes() / (double) entity.getDureeMensuelleMinutes();
            double prixMensualiseSurAnnee = entity.getPrixMensuel().doubleValue() * moisEquivalents;
            if (prixMensualiseSurAnnee > 0) {
                reduction = 1.0 - (entity.getPrixAnnuel().doubleValue() / prixMensualiseSurAnnee);
            }
        }
        return Offre.builder()
                .id(entity.getId())
                .nom(entity.getNom())
                .description(entity.getDescription())
                .cible(entity.getCible())
                .prixMensuel(entity.getPrixMensuel())
                .dureeMensuelleMinutes(entity.getDureeMensuelleMinutes())
                .prixAnnuel(entity.getPrixAnnuel())
                .dureeAnnuelleMinutes(entity.getDureeAnnuelleMinutes())
                .nombreClassesInclues(entity.getNombreClassesInclues())
                .classesBonus(entity.getClassesBonus())
                .estTest(entity.isEstTest())
                .actif(entity.isActif())
                .dateCreation(entity.getDateCreation())
                .dateMaj(entity.getDateMaj())
                .createdBy(entity.getCreatedBy())
                .reductionAnnuellePourcentage(reduction)
                .delaiRappelSuppressionMinutes(entity.getDelaiRappelSuppressionMinutes())
                .delaiSuppressionMinutes(entity.getDelaiSuppressionMinutes())
                .elevesMax(entity.getElevesMax())
                .stockageMaxGo(entity.getStockageMaxGo())
                .messagerieIncluse(entity.getMessagerieIncluse())
                .build();
    }
}
