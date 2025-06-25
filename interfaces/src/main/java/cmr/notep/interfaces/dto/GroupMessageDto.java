package cmr.notep.interfaces.dto;

import lombok.Data;

@Data
public class GroupMessageDto {
    private String classId;
    private String content;
    private String senderId;
}