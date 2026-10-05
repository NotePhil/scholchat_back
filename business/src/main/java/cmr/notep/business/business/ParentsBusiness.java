package cmr.notep.business.business;

import cmr.notep.business.security.UserSubtypeService;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.dto.ParentSummaryDto;
import cmr.notep.interfaces.modeles.Eleves;
import cmr.notep.interfaces.modeles.Parents;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.ElevesEntity;
import cmr.notep.ressourcesjpa.dao.ParentsEntity;
import cmr.notep.ressourcesjpa.repository.ElevesRepository;
import cmr.notep.ressourcesjpa.repository.ParentEleveRepository;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import cmr.notep.ressourcesjpa.repository.ParentsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
public class ParentsBusiness {
    private final UserSubtypeService userSubtypeService;
    private final DaoAccessorService daoAccessorService;

    public ParentsBusiness(DaoAccessorService daoAccessorService,
            UserSubtypeService userSubtypeService) {
        this.userSubtypeService = userSubtypeService;
        this.daoAccessorService = daoAccessorService;
    }

    public Parents avoirParent(String idParent) {
        log.info("avoirParent called");
        return dozerMapperBean.map(
                userSubtypeService.findSubtype(ParentsEntity.class, idParent)
                        .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND,"Parent introuvable avec l'ID: " + idParent)),
                Parents.class
        );
    }

    public Parents posterParent(Parents Parent) {
        return dozerMapperBean.map(
                this.daoAccessorService.getRepository(ParentsRepository.class)
                        .save(dozerMapperBean.map(Parent, ParentsEntity.class)),
                Parents.class
        );
    }

    public List<Parents> avoirToutParents() {
        return daoAccessorService.getRepository(ParentsRepository.class).findAll()
                .stream()
                .map(parent -> dozerMapperBean.map(parent, Parents.class))
                .collect(Collectors.toList());
    }

    public List<ParentSummaryDto> avoirToutParentsSummary() {
        return daoAccessorService.getRepository(ParentsRepository.class).findAll()
                .stream()
                .map(p -> ParentSummaryDto.builder()
                        .id(p.getId())
                        .nom(p.getNom())
                        .prenom(p.getPrenom())
                        .email(p.getEmail())
                        .telephone(p.getTelephone())
                        .adresse(p.getAdresse())
                        .etat(p.getEtat())
                        .creationDate(p.getCreationDate())
                        .admin(p.getAdmin() != null && p.getAdmin())
                        .build())
                .collect(Collectors.toList());
    }

    public Parents modifierParentPartiellement(String idParent, Parents partialParent) {
        log.info("modifierParentPartiellement called for ID: {}", idParent);

        ParentsRepository repository = daoAccessorService.getRepository(ParentsRepository.class);
        ParentsEntity existingEntity = userSubtypeService.findSubtype(ParentsEntity.class, idParent)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Parent introuvable avec l'ID : " + idParent));

        // Update only non-null fields from partialParent
        Parents existingParent = dozerMapperBean.map(existingEntity, Parents.class);

        if (partialParent.getNom() != null) {
            existingParent.setNom(partialParent.getNom());
        }
        if (partialParent.getPrenom() != null) {
            existingParent.setPrenom(partialParent.getPrenom());
        }
        if (partialParent.getEmail() != null) {
            existingParent.setEmail(partialParent.getEmail().toLowerCase());
        }
        if (partialParent.getTelephone() != null) {
            existingParent.setTelephone(partialParent.getTelephone());
        }
        if (partialParent.getAdresse() != null) {
            existingParent.setAdresse(partialParent.getAdresse());
        }
        if (partialParent.getEtat() != null) {
            existingParent.setEtat(partialParent.getEtat());
        }

        // Save the updated entity
        ParentsEntity updatedEntity = repository.save(dozerMapperBean.map(existingParent, ParentsEntity.class));
        return dozerMapperBean.map(updatedEntity, Parents.class);
    }


    // Les liens parent/enfant sont lus et écrits directement dans parent_eleve : un parent peut
    // aussi être professeur ou élève (compte multi-rôles), et Hibernate ne garde qu'un sous-type
    // par id dans le contexte de persistance — ParentsRepository.findById(id) peut alors renvoyer
    // vide alors que la ligne parents existe bien.

    private void verifierParent(String parentId) {
        if (parentId == null || !daoAccessorService.getRepository(UtilisateursRepository.class).hasParentRow(parentId)) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Parent introuvable");
        }
    }

    private List<ElevesEntity> enfantsDe(String parentId) {
        List<String> ids = daoAccessorService.getRepository(ParentEleveRepository.class).findEleveIdsByParentId(parentId);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        return daoAccessorService.getRepository(ElevesRepository.class).findAllById(ids);
    }

    public void ajouterEnfant(String parentId, String eleveId) throws SchoolException {
        verifierParent(parentId);
        ElevesEntity eleve = daoAccessorService.getRepository(ElevesRepository.class)
                .findById(eleveId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Élève introuvable"));

        ParentEleveRepository liens = daoAccessorService.getRepository(ParentEleveRepository.class);
        if (liens.existsByParentIdAndEleveId(parentId, eleveId)) {
            return;
        }

        // Duplicate check: same nom + prenom + niveau (case-insensitive)
        boolean duplicate = enfantsDe(parentId).stream().anyMatch(e ->
                e.getNom() != null && e.getNom().equalsIgnoreCase(eleve.getNom()) &&
                e.getPrenom() != null && e.getPrenom().equalsIgnoreCase(eleve.getPrenom()) &&
                e.getNiveau() != null && e.getNiveau().equalsIgnoreCase(eleve.getNiveau())
        );
        if (duplicate) {
            throw new SchoolException(SchoolErrorCode.ALREADY_EXISTS,
                    "Un enfant nommé " + eleve.getPrenom() + " " + eleve.getNom() +
                    " en " + eleve.getNiveau() + " est déjà associé à ce compte.");
        }

        liens.insertLien(parentId, eleveId);
    }

    public void retirerEnfant(String parentId, String eleveId) throws SchoolException {
        verifierParent(parentId);
        if (!daoAccessorService.getRepository(ElevesRepository.class).existsById(eleveId)) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Élève introuvable");
        }
        daoAccessorService.getRepository(ParentEleveRepository.class).deleteLien(parentId, eleveId);
    }

    public List<ParentSummaryDto> avoirParentsPourProfesseur(String professeurId) {
        return daoAccessorService.getRepository(ParentsRepository.class)
                .findParentsByProfesseurId(professeurId)
                .stream()
                .map(p -> ParentSummaryDto.builder()
                        .id(p.getId())
                        .nom(p.getNom())
                        .prenom(p.getPrenom())
                        .email(p.getEmail())
                        .telephone(p.getTelephone())
                        .adresse(p.getAdresse())
                        .etat(p.getEtat())
                        .creationDate(p.getCreationDate())
                        .admin(p.getAdmin() != null && p.getAdmin())
                        .build())
                .collect(Collectors.toList());
    }

    public List<Eleves> obtenirEnfants(String parentId) throws SchoolException {
        verifierParent(parentId);
        return enfantsDe(parentId).stream()
                .map(e -> dozerMapperBean.map(e, Eleves.class))
                .collect(Collectors.toList());
    }
}