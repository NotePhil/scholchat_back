package cmr.notep.business.config;

import jakarta.annotation.PostConstruct;
import org.dozer.DozerBeanMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "cmr.notep.ressourcesjpa.repository")
public class BusinessConfig {
    public static DozerBeanMapper dozerMapperBean;
    @PostConstruct
    void init(){
        dozerMapperBean = new DozerBeanMapper();
    }
}
