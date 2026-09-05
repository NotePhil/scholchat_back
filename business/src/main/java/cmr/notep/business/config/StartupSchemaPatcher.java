package cmr.notep.business.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Self-healing schema patch for gaps not (yet) captured by an automatic
 * migration runner. This exists because the dev database container used
 * during development runs on an ephemeral volume that gets wiped on every
 * restart, silently reverting any migration applied by hand — the
 * media.question_id column (added for question attachments) kept
 * disappearing and breaking uploads. Re-applying it here, on every
 * application startup, makes it self-healing regardless of how or when the
 * database gets (re)provisioned.
 *
 * Every statement is intentionally idempotent (IF NOT EXISTS, or an
 * existence check before adding a constraint) and executed independently,
 * so a statement that's already applied — or fails for any reason — never
 * blocks another fix or application startup.
 */
@Component
@Slf4j
public class StartupSchemaPatcher implements ApplicationRunner {

    private static final String[] IDEMPOTENT_STATEMENTS = {
        "ALTER TABLE ressources.media ADD COLUMN IF NOT EXISTS question_id VARCHAR(255)",
        "CREATE INDEX IF NOT EXISTS idx_media_question_id ON ressources.media(question_id)",
    };

    private final DataSource dataSource;

    public StartupSchemaPatcher(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            for (String sql : IDEMPOTENT_STATEMENTS) {
                try {
                    stmt.execute(sql);
                } catch (Exception e) {
                    log.warn("Startup schema patch statement failed (likely fine, already applied): {} — {}", sql, e.getMessage());
                }
            }

            // FK constraints aren't naturally idempotent in Postgres (no
            // "ADD CONSTRAINT IF NOT EXISTS") — check first.
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT 1 FROM information_schema.table_constraints " +
                    "WHERE constraint_schema = 'ressources' AND table_name = 'media' " +
                    "AND constraint_name = 'fk_media_question'")) {
                if (!rs.next()) {
                    stmt.execute(
                        "ALTER TABLE ressources.media ADD CONSTRAINT fk_media_question " +
                        "FOREIGN KEY (question_id) REFERENCES ressources.questions_reponses(id) ON DELETE CASCADE");
                    log.info("Startup schema patch: added fk_media_question constraint");
                }
            } catch (Exception e) {
                log.warn("Could not verify/add fk_media_question constraint: {}", e.getMessage());
            }

            log.info("Startup schema patch complete (media.question_id column/index/constraint).");
        } catch (Exception e) {
            log.error("Startup schema patch failed entirely — question media uploads may not work: {}", e.getMessage());
        }
    }
}
