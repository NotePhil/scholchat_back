package cmr.notep.interfaces.modeles;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MotifRejet {
    private String id;
    private String code;
    private String descriptif;
    private LocalDateTime dateCreation;
}