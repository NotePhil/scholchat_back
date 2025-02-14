package cmr.notep.business.config;

import jakarta.annotation.PostConstruct;
import org.dozer.CustomConverter;
import org.dozer.DozerBeanMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableAsync
@EnableJpaRepositories(basePackages = "cmr.notep.ressourcesjpa.repository")
public class BusinessConfig {
    public static DozerBeanMapper dozerMapperBean;
    @PostConstruct
    void init() {
        synchronized (BusinessConfig.class) {
            if (dozerMapperBean == null) {
                dozerMapperBean = new DozerBeanMapper();

                // Load the mapping file
                List<String> mappingFiles = new ArrayList<>();
                mappingFiles.add("dozer-mappings.xml");
                dozerMapperBean.setMappingFiles(mappingFiles);

                // Register the custom converter
                List<CustomConverter> converters = new ArrayList<>();
                converters.add(new LocalDateTimeConverter());
                dozerMapperBean.setCustomConverters(converters);
            }
        }
    }
}
