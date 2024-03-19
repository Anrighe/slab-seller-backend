package repository.dao;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;
import repository.model.SlabEntity;

import java.util.List;
import java.util.Optional;

public interface SlabDAO {

    @SqlQuery("SELECT * FROM slab")
    @RegisterBeanMapper(SlabEntity.class)
    List<SlabEntity> getAll();

    @SqlQuery("SELECT * FROM slab WHERE id = :id")
    @RegisterBeanMapper(SlabEntity.class)
    Optional<SlabEntity> getById(@BindBean String id);

    @Transaction
    @SqlUpdate("UPDATE slab SET " +
            "collection= :collection, " +
            "color= :color, " +
            "tone= :tone " +
            "width= :width " +
            "height= :height " +
            "price_id= :price_id " +
            "WHERE id = :id")
    void update(@BindBean SlabEntity slab);
}
