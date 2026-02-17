package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.Notification;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/notifications")
public interface NotificationApi {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    List<Notification> getUserNotifications();

    @GetMapping(path = "/unread", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    List<Notification> getUnreadNotifications();

    @GetMapping(path = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    Long getUnreadCount();

    @PatchMapping(path = "/{id}/read", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    Notification markAsRead(@NonNull @PathVariable("id") String id);

    @PatchMapping(path = "/read-all", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    void markAllAsRead();

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteNotification(@NonNull @PathVariable("id") String id);

    @DeleteMapping("/all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteAllNotifications();
}
