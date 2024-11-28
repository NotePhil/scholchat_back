package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.Classes;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface ClassesApi {

    @PostMapping("/classes")
    Classes createClass(@RequestBody Classes classes);

    @PutMapping("/classes/{id}")
    Classes updateClass(@PathVariable String id, @RequestBody Classes updatedClass);

    @DeleteMapping("/classes/{id}")
    void deleteClass(@PathVariable String id);

    @GetMapping("/classes/{id}")
    Classes getClassById(@PathVariable String id);

    @GetMapping("/classes")
    List<Classes> getAllClasses();
}
