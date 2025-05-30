package cmr.notep.interfaces.api;

import cmr.notep.interfaces.dto.MediaDto;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("/media")
public interface MediaApi {

    @PostMapping(
            path = "/presigned-url",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    Map<String, String> generatePresignedUrl(@RequestBody Map<String, String> request);

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