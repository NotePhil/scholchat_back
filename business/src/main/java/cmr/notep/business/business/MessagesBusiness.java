package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Messages;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.MessagesEntity;
import cmr.notep.ressourcesjpa.repository.MessagesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
public class MessagesBusiness {
    private final DaoAccessorService daoAccessorService ;

    public MessagesBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    public Messages avoirMessage(String idMessage) throws SchoolException {
        return dozerMapperBean.map(daoAccessorService.getRepository(MessagesRepository.class)
                .findById(idMessage)
                .orElseThrow(()-> new SchoolException(SchoolErrorCode.NOT_FOUND, "Message introuvable avec l'ID: " + idMessage)),Messages.class);
    }

    public Messages posterMessage(Messages message) {
        return dozerMapperBean.map(this.daoAccessorService.getRepository(MessagesRepository.class)
                .save(dozerMapperBean.map(message, MessagesEntity.class)), Messages.class);

    }

    public List<Messages> avoirToutMessages() {
        return daoAccessorService.getRepository(MessagesRepository.class).findAll()
                .stream().map(msg -> dozerMapperBean.map(msg,Messages.class))
                .collect(Collectors.toList());
    }
}
