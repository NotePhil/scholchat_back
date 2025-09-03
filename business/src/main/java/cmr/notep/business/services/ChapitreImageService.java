//package cmr.notep.business.services;
//
//import cmr.notep.business.business.MediaBusiness;
//import cmr.notep.business.exceptions.SchoolException;
//import cmr.notep.business.exceptions.enums.SchoolErrorCode;
//import cmr.notep.ressourcesjpa.dao.ChapitreEntity;
//import cmr.notep.ressourcesjpa.repository.ChapitreRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class ChapitreImageService {
//
//    private final MediaBusiness mediaBusiness;
//    private final ChapitreRepository chapitreRepository;
//
//    public String uploadChapitreImage(String chapitreId, String fileName,
//                                      byte[] imageData, String contentType,
//                                      String ownerId) {
//        try {
//            // Générer l'URL de upload
//            String uploadUrl = mediaBusiness.generateUploadUrl(
//                    fileName,
//                    contentType,
//                    "PHOTOS",           // mediaType
//                    ownerId,            // ownerId (l'utilisateur qui crée le cours)
//                    "CHAPTER_IMAGES"    // documentType spécifique aux images de chapitres
//            );
//
//            // Uploader l'image
//            // (implémentez la logique d'upload ici)
//
//            // Sauvegarder la référence dans le chapitre
//            ChapitreEntity chapitre = chapitreRepository.findById(chapitreId)
//                    .orElseThrow(() -> new SchoolException(
//                            SchoolErrorCode.NOT_FOUND,
//                            "Chapitre non trouvé"
//                    ));
//
//            chapitre.setImageMediaId(mediaId); // L'ID du média après upload
//            chapitreRepository.save(chapitre);
//
//            return mediaId;
//
//        } catch (Exception e) {
//            throw new SchoolException(
//                    SchoolErrorCode.INTERNAL_ERROR,
//                    "Erreur lors de l'upload de l'image du chapitre"
//            );
//        }
//    }
//
//    public String getChapitreImageUrl(String chapitreId) {
//        ChapitreEntity chapitre = chapitreRepository.findById(chapitreId)
//                .orElseThrow(() -> new SchoolException(
//                        SchoolErrorCode.NOT_FOUND,
//                        "Chapitre non trouvé"
//                ));
//
//        if (chapitre.getImageMediaId() == null) {
//            return null;
//        }
//
//        // Récupérer l'URL de téléchargement
//        return mediaBusiness.generateDownloadUrl(chapitre.getImageMediaId());
//    }
//}