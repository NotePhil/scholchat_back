package cmr.notep.business;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.retry.annotation.EnableRetry;

import java.time.ZoneOffset;
import java.util.TimeZone;

@SpringBootApplication(scanBasePackages = "cmr.notep")
@EntityScan(basePackages = "cmr.notep.ressourcesjpa.dao")
@EnableScheduling
@EnableRetry
public class BusinessApplication {
    public static void main(String[] args) {
        // The server's canonical zone is UTC, whatever the host's zone is: every
        // LocalDateTime/Date the application creates or reads from the database
        // is a UTC instant, and the API writes date-times as ISO "...Z" strings
        // (see config.time.ServerDateTimes). Clients convert to the viewer's zone.
        // The property matters too: TimeZone.setDefault(null) (called by some libraries)
        // falls back to "user.timezone", which is the host zone unless overridden.
        System.setProperty("user.timezone", "UTC");
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
        SpringApplication.run(BusinessApplication.class, args);
    }

}
