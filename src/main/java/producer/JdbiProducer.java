package producer;

import jakarta.enterprise.context.*;
import lombok.extern.slf4j.*;
import org.jdbi.v3.core.*;
import org.jdbi.v3.core.statement.*;
import org.jdbi.v3.sqlobject.*;

import javax.sql.*;
import java.sql.*;
import java.time.temporal.*;

/**
 * Classe che inizializza Jdbi passando in input un datasource
 */
@Slf4j
@ApplicationScoped
public class JdbiProducer {

    public Jdbi getJdbi(DataSource dataSource) {
        Jdbi jdbi = Jdbi.create(dataSource).installPlugin(new SqlObjectPlugin());
        configureJdbi(jdbi);

        return jdbi;
    }

    /**
     * @param jdbi
     */
    private void configureJdbi(Jdbi jdbi) {
        jdbi.setSqlLogger(new SqlLogger() {

            @Override
            public void logBeforeExecution(StatementContext context) {
                if (log.isDebugEnabled()) {
                    log.debug("Execute query: " + context.getRenderedSql());
                    log.debug(context.getBinding().toString());
                }
            }

            @Override
            public void logAfterExecution(StatementContext context) {
                if (log.isDebugEnabled()) {
                    log.debug("Query time: " + context.getElapsedTime(ChronoUnit.MILLIS) + " ms.");
                }
            }

            @Override
            public void logException(StatementContext context, SQLException ex) {
                log.error("Error on execute query " + context.getRenderedSql() + " Error:", ex);
            }
        });
    }
}
