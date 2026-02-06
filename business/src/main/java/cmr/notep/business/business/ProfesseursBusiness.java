package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.services.ActivationEmailService;
import cmr.notep.business.utils.JwtUtil;
import cmr.notep.interfaces.modeles.Classes;
import cmr.notep.interfaces.modeles.Professeurs;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.ProfesseursEntity;
import cmr.notep.ressourcesjpa.repository.ProfesseursRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
@Transactional
public class ProfesseursBusiness {
    private final DaoAccessorService daoAccessorService;
    private final ActivationEmailService activationEmailService;
    private final JwtUtil jwtUtil;
    public ProfesseursBusiness(DaoAccessorService daoAccessorService, ActivationEmailService activationEmailService, JwtUtil jwtUtil) {
        this.daoAccessorService = daoAccessorService;
        this.activationEmailService = activationEmailService;
        this.jwtUtil = jwtUtil;
    }

    public Professeurs avoirProfesseur(String idProfesseur) {
        ProfesseursEntity entity = daoAccessorService.getRepository(ProfesseursRepository.class)
                .findById(idProfesseur)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Professeur introuvable avec l'ID: " + idProfesseur));

        Professeurs professeur = dozerMapperBean.map(entity, Professeurs.class);

        // Map moderated classes to simple IDs
        professeur.setModeratedClasses(
                entity.getModeratedClasses().stream()
                        .map(c -> {
                            Classes cls = new Classes();
                            cls.setId(c.getId());
                            cls.setNom(c.getNom());
                            return cls;
                        })
                        .collect(Collectors.toList())
        );

        return professeur;
    }

    public Professeurs posterProfesseur(Professeurs professeur) {
        return dozerMapperBean.map(
                this.daoAccessorService.getRepository(ProfesseursRepository.class)
                        .save(dozerMapperBean.map(professeur, ProfesseursEntity.class)),
                Professeurs.class
        );
    }

    public Professeurs modifierProfesseurPartiellement(String idProfesseur, Professeurs partialUpdate) {
        ProfesseursRepository repository = daoAccessorService.getRepository(ProfesseursRepository.class);

        // Get existing professor
        ProfesseursEntity existingEntity = repository.findById(idProfesseur)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Professeur introuvable avec l'ID: " + idProfesseur));

        // Map the existing entity to model
        Professeurs existingProfesseur = dozerMapperBean.map(existingEntity, Professeurs.class);

        // Apply partial updates
        if (partialUpdate.getNom() != null) {
            existingProfesseur.setNom(partialUpdate.getNom());
        }
        if (partialUpdate.getPrenom() != null) {
            existingProfesseur.setPrenom(partialUpdate.getPrenom());
        }
        if (partialUpdate.getEmail() != null) {
            existingProfesseur.setEmail(partialUpdate.getEmail());
        }
        if (partialUpdate.getTelephone() != null) {
            existingProfesseur.setTelephone(partialUpdate.getTelephone());
        }
        if (partialUpdate.getAdresse() != null) {
            existingProfesseur.setAdresse(partialUpdate.getAdresse());
        }
        if (partialUpdate.getEtat() != null) {
            existingProfesseur.setEtat(partialUpdate.getEtat());
        }
        if (partialUpdate.getCniUrlRecto() != null) {
            existingProfesseur.setCniUrlRecto(partialUpdate.getCniUrlRecto());
        }
        if (partialUpdate.getCniUrlVerso() != null) {
            existingProfesseur.setCniUrlVerso(partialUpdate.getCniUrlVerso());
        }
        if (partialUpdate.getSelfieUrl() != null) {
            existingProfesseur.setSelfieUrl(partialUpdate.getSelfieUrl());
        }
        if (partialUpdate.getMatriculeProfesseur() != null) {
            existingProfesseur.setMatriculeProfesseur(partialUpdate.getMatriculeProfesseur());
        }

        // Update hasUploaded status based on document presence
        boolean hasUploaded = existingProfesseur.getCniUrlRecto() != null &&
                existingProfesseur.getCniUrlVerso() != null &&
                existingProfesseur.getSelfieUrl() != null;
        existingProfesseur.setHasUploaded(hasUploaded);

        // Save the updated entity
        ProfesseursEntity updatedEntity = repository.save(dozerMapperBean.map(existingProfesseur, ProfesseursEntity.class));

        // Check if we need to send activation email after upload
        boolean wasNotUploaded = !existingEntity.getHasUploaded();
        boolean nowHasUploaded = hasUploaded;

        if (wasNotUploaded && nowHasUploaded) {
            // Professor just completed uploads - send activation email
            List<String> roles = new ArrayList<>();
            roles.add("ROLE_PROFESSOR");

            String activationToken = jwtUtil.generateAccessToken(existingProfesseur.getEmail(), roles);
            existingProfesseur.setActivationToken(activationToken);

            // Update entity with activation token
            updatedEntity.setActivationToken(activationToken);
            updatedEntity = repository.save(updatedEntity);

            // Send activation email
            activationEmailService.sendActivationEmail(existingProfesseur, activationToken);
        }

        return dozerMapperBean.map(updatedEntity, Professeurs.class);
    }
    public List<Professeurs> avoirToutProfesseurs() {
        return daoAccessorService.getRepository(ProfesseursRepository.class).findAll()
                .stream()
                .map(prof -> dozerMapperBean.map(prof, Professeurs.class))
                .collect(Collectors.toList());
    }

    public Professeurs avoirProfesseurParMatricule(String matriculeProfesseur) {
        return dozerMapperBean.map(
                daoAccessorService.getRepository(ProfesseursRepository.class)
                        .findByMatriculeProfesseur(matriculeProfesseur),
                Professeurs.class
        );
    }
}