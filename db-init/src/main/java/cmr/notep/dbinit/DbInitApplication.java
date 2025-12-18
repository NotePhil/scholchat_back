package cmr.notep.dbinit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DbInitApplication {

    public static void main(String[] args) {
        // Single instance semantics are handled at IaC / deployment level (one-off job)
        SpringApplication.run(DbInitApplication.class, args);
    }
}

