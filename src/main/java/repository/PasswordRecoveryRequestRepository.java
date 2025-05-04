package repository;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import producer.JdbiProducer;
import repository.dao.PasswordRecoveryRequestDAO;
import repository.model.PasswordRecoveryRequestEntity;

import java.util.List;

@Slf4j
@Data
@ApplicationScoped
public class PasswordRecoveryRequestRepository {

    @Inject
    AgroalDataSource defaultDataSource;

    @Inject
    JdbiProducer jdbiProducer;

    public List<PasswordRecoveryRequestEntity> getAllProductTypes() {
        Jdbi jdbi = jdbiProducer.getJdbi(defaultDataSource);
        return jdbi.withExtension(PasswordRecoveryRequestDAO.class, PasswordRecoveryRequestDAO::getAllPasswordRecoveryRequests);
    }

    public boolean insertPasswordRecoveryRequest(PasswordRecoveryRequestEntity entitytoInsert) {

        Jdbi jdbi = jdbiProducer.getJdbi(defaultDataSource);

        return jdbi.withExtension(PasswordRecoveryRequestDAO.class, dao -> dao.insertPasswordRecoveryRequest(entitytoInsert));
    }

    public boolean invalidatePasswordRecoveryRequestsForEmail(PasswordRecoveryRequestEntity entity) {
        Jdbi jdbi = jdbiProducer.getJdbi(defaultDataSource);

        return jdbi.withExtension(PasswordRecoveryRequestDAO.class, dao -> dao.invalidatePasswordRecoveryRequestsForEmail(entity));
    }

    public List<PasswordRecoveryRequestEntity> getAllPasswordRecoveryRequestForEmail(PasswordRecoveryRequestEntity entity) {
        Jdbi jdbi = jdbiProducer.getJdbi(defaultDataSource);

        return jdbi.withExtension(PasswordRecoveryRequestDAO.class, dao -> dao.getAllPasswordRecoveryRequestForEmail(entity));
    }

    public PasswordRecoveryRequestEntity getPasswordRecoveryRequestByHashedId(String hashedId) {
        Jdbi jdbi = jdbiProducer.getJdbi(defaultDataSource);

        return jdbi.withExtension(PasswordRecoveryRequestDAO.class, dao -> dao.getPasswordRecoveryRequestByHashedId(hashedId));
    }
}