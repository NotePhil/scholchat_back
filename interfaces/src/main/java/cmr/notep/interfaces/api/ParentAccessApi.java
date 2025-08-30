package cmr.notep.interfaces.api;

import cmr.notep.interfaces.dto.ClasseInfoDto;
import cmr.notep.interfaces.dto.ParentAccessRequestDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/parent-access")
public interface ParentAccessApi {

    @GetMapping(
            path = "/infos-classe",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    ClasseInfoDto validerTokenEtRecupererInfos(
            @RequestParam String token,
            @RequestParam String classId
    );

    @PostMapping(
            path = "/demande",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    void traiterDemandeAcces(@RequestBody ParentAccessRequestDto request);
}
