package repository;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import producer.JdbiProducer;
import repository.dao.SlabDAO;
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

    public List<SlabEntity> getAll() {
        Jdbi jdbi = jdbiProducer.getJdbi(defaultDataSource);
        return jdbi.withExtension(SlabDAO.class, SlabDAO::getAll);
    }

    public Optional<SlabEntity> getById(String id) {
        Jdbi jdbi = jdbiProducer.getJdbi(defaultDataSource);
        return jdbi.withExtension(SlabDAO.class, slabDAO -> slabDAO.getById(id));
    }
}
