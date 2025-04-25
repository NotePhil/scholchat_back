package cmr.notep.interfaces.api;

import cmr.notep.interfaces.dto.MediaDto;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/media")
public interface MediaApi {

    @PostMapping(
            path = "/presigned-url",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    MediaDto generatePresignedUrl(@RequestBody MediaDto mediaDto);

    @GetMapping(
            path = "/{mediaId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    MediaDto getMediaById(@PathVariable String mediaId);

    @GetMapping(
            path = "/download/{mediaId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    MediaDto generateDownloadUrl(@PathVariable String mediaId);

    @GetMapping(
            path = "/user/{userId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<MediaDto> getMediaByUserId(@PathVariable String userId);

    @DeleteMapping(
            path = "/{mediaId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    void deleteMedia(@PathVariable String mediaId);
}