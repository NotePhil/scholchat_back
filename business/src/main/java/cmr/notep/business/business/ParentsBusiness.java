package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.dto.ParentSummaryDto;
import cmr.notep.interfaces.modeles.Eleves;
import cmr.notep.interfaces.modeles.Parents;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.ElevesEntity;
import cmr.notep.ressourcesjpa.dao.ParentsEntity;
import cmr.notep.ressourcesjpa.repository.ElevesRepository;
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
    private final DaoAccessorService daoAccessorService;

    public ParentsBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    public Parents avoirParent(String idParent) {
        log.info("avoirParent called");
        return dozerMapperBean.map(
                daoAccessorService.getRepository(ParentsRepository.class)
                        .findById(idParent)
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
        ParentsEntity existingEntity = repository.findById(idParent)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Parent introuvable avec l'ID: " + idParent));

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


    public void ajouterEnfant(String parentId, String eleveId) throws SchoolException {
        ParentsEntity parent = daoAccessorService.getRepository(ParentsRepository.class)
                .findById(parentId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Parent introuvable"));

        ElevesEntity eleve = daoAccessorService.getRepository(ElevesRepository.class)
                .findById(eleveId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Élève introuvable"));

        if (parent.getEnfants() == null) {
            parent.setEnfants(new ArrayList<>());
        }

        if (!parent.getEnfants().contains(eleve)) {
            parent.getEnfants().add(eleve);
            daoAccessorService.getRepository(ParentsRepository.class).save(parent);
        }
    }

    public void retirerEnfant(String parentId, String eleveId) throws SchoolException {
        ParentsEntity parent = daoAccessorService.getRepository(ParentsRepository.class)
                .findById(parentId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Parent introuvable"));

        ElevesEntity eleve = daoAccessorService.getRepository(ElevesRepository.class)
                .findById(eleveId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Élève introuvable"));

        if (parent.getEnfants() != null && parent.getEnfants().contains(eleve)) {
            parent.getEnfants().remove(eleve);
            daoAccessorService.getRepository(ParentsRepository.class).save(parent);
        }
    }

    public List<Eleves> obtenirEnfants(String parentId) throws SchoolException {
        ParentsEntity parent = daoAccessorService.getRepository(ParentsRepository.class)
                .findById(parentId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Parent introuvable"));

        if (parent.getEnfants() == null) {
            return Collections.emptyList();
        }

        return parent.getEnfants().stream()
                .map(e -> dozerMapperBean.map(e, Eleves.class))
                .collect(Collectors.toList());
    }
}