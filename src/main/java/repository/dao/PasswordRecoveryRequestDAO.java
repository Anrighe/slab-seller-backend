package repository.dao;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import repository.model.PasswordRecoveryRequestEntity;

import java.util.List;

public interface PasswordRecoveryRequestDAO {

    @SqlQuery("SELECT * FROM password_recovery_request")
    @RegisterBeanMapper(PasswordRecoveryRequestEntity.class)
    List<PasswordRecoveryRequestEntity> getAllPasswordRecoveryRequests();

    @SqlUpdate("INSERT INTO password_recovery_request (email, hashed_id) VALUES (:email, :hashedId)")
    @RegisterBeanMapper(PasswordRecoveryRequestEntity.class)
    boolean insertPasswordRecoveryRequest(@BindBean PasswordRecoveryRequestEntity entity);

    @SqlUpdate("UPDATE password_recovery_request SET disabled = true WHERE email = :email")
    @RegisterBeanMapper(PasswordRecoveryRequestEntity.class)
    boolean invalidatePasswordRecoveryRequestsForEmail(@BindBean PasswordRecoveryRequestEntity entity);

    @SqlQuery("SELECT * FROM password_recovery_request WHERE email = :email")
    @RegisterBeanMapper(PasswordRecoveryRequestEntity.class)
    List<PasswordRecoveryRequestEntity> getAllPasswordRecoveryRequestForEmail(@BindBean PasswordRecoveryRequestEntity entity);

    @SqlQuery("SELECT * FROM password_recovery_request WHERE hashed_id = :hashedId")
    @RegisterBeanMapper(PasswordRecoveryRequestEntity.class)
    PasswordRecoveryRequestEntity getPasswordRecoveryRequestByHashedId(@Bind("hashedId") String hashedId);
}
