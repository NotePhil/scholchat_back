package cmr.notep.dbinit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@Slf4j
@SpringBootApplication
public class DbInitApplication {

    public static void main(String[] args) {
        // Single instance semantics are handled at IaC / deployment level (one-off job)
       // SpringApplication.run(DbInitApplication.class, args);
        System.exit(SpringApplication.exit(SpringApplication.run(DbInitApplication.class, args)));
    }

    public void run(String... args) throws Exception {
        log.info("Database initialization completed successfully");
        // L'application se fermera automatiquement après ce point
    }
}

