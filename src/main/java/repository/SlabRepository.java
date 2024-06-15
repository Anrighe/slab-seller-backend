package repository;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import producer.JdbiProducer;
import repository.dao.SlabDAO;
import repository.model.PricedSlabAvailabilityEntity;
import repository.model.SlabEntity;

import java.util.*;

@Slf4j
@Data
@ApplicationScoped
public class SlabRepository {

    @Inject
    AgroalDataSource defaultDataSource;

    @Inject
    JdbiProducer jdbiProducer;

    public List<SlabEntity> getAllProductTypes() {
        Jdbi jdbi = jdbiProducer.getJdbi(defaultDataSource);
        return jdbi.withExtension(SlabDAO.class, SlabDAO::getAllProductTypes);
    }

    public Optional<SlabEntity> getProductTypeById(String id) {
        Jdbi jdbi = jdbiProducer.getJdbi(defaultDataSource);
        return jdbi.withExtension(SlabDAO.class, slabDAO -> slabDAO.getProductTypeById(id));
    }

    public List<PricedSlabAvailabilityEntity> getAllProductsWithDetailsAndAvailability() {
        Jdbi jdbi = jdbiProducer.getJdbi(defaultDataSource);
        return jdbi.withExtension(SlabDAO.class, SlabDAO::getAllProductsWithDetailsAndAvailability);
    }
}
