package cmr.notep.business.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PendingPurgeConfig {
    @Value("${pending.purge.days}")
    private int purgeDays;

    public int getPurgeDays() {
        return purgeDays;
    }
}
