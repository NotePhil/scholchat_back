package cmr.notep.interfaces.dto;

import lombok.Data;

import java.util.List;

@Data
public class GroupMessageDto {
    private List<String> classIds;
    private String objet;
    private String content;
    private String senderId;
    private List<String> copieRecipientIds;
    /** Pièces jointes (fileName, filePath, contentType, fileSize). */
    private List<cmr.notep.interfaces.modeles.Media> medias;
}