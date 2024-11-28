package cmr.notep.business.impl;

import cmr.notep.business.business.ClassesBusiness;
import cmr.notep.interfaces.api.ClassesApi;
import cmr.notep.interfaces.modeles.Classes;
import lombok.NonNull;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ClassesService implements ClassesApi {

    private final ClassesBusiness classesBusiness;

    public ClassesService(ClassesBusiness classesBusiness) {
        this.classesBusiness = classesBusiness;
    }

    @Override
    public Classes createClass(@NonNull Classes classes) {
        return classesBusiness.createClass(classes);
    }

    @Override
    public Classes updateClass(@NonNull String id, @NonNull Classes updatedClass) {
        return classesBusiness.updateClass(id, updatedClass);
    }

    @Override
    public void deleteClass(@NonNull String id) {
        classesBusiness.deleteClass(id);
    }

    @Override
    public Classes getClassById(@NonNull String id) {
        return classesBusiness.getClassById(id);
    }

    @Override
    public List<Classes> getAllClasses() {
        return classesBusiness.getAllClasses();
    }
}
