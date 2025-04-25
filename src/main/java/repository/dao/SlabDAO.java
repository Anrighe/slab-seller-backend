package repository.dao;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;
import repository.model.PricedSlabAvailabilityEntity;
import repository.model.SlabEntity;

import java.util.List;
import java.util.Optional;

public interface SlabDAO {

    // TODO: check if with * still works
    @SqlQuery("SELECT * FROM slab")
    @RegisterBeanMapper(SlabEntity.class)
    List<SlabEntity> getAllProductTypes();


    @SqlQuery("SELECT * FROM slab ORDER BY id ASC LIMIT :limit OFFSET :offset")
    @RegisterBeanMapper(SlabEntity.class)
    List<SlabEntity> getProductTypesPaged(@Bind("limit") int limit, @Bind("offset") int offset);

    @SqlQuery("SELECT * FROM slab WHERE id = :id")
    @RegisterBeanMapper(SlabEntity.class)
    Optional<SlabEntity> getProductTypeById(@BindBean String id);

    @SqlQuery("SELECT s.id, s.collection, s.color, s.tone, s.width, s.height, p.price, p.currency, a.quantity " +
              "FROM slab s " +
              "JOIN pricelist p on s.price_id = p.id " +
              "JOIN availability a on s.id = a.id")
    @RegisterBeanMapper(PricedSlabAvailabilityEntity.class)
    List<PricedSlabAvailabilityEntity> getAllProductsWithDetailsAndAvailability();

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
