package service;

import controller.dto.SlabDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import mapper.SlabMapper;
import repository.SlabRepository;
import repository.model.SlabEntity;

import java.util.List;
import java.util.Optional;

@Slf4j
@Data
@ApplicationScoped
public class SlabService {

    @Inject
    SlabRepository slabRepository;

    @Inject
    SlabMapper slabMapper;

    public List<SlabDTO> getAll() {
        List<SlabEntity> all = slabRepository.getAll();
        return slabMapper.toDtos(all);
    }

    public SlabDTO getById(String id) {
        Optional<SlabEntity> byId = slabRepository.getById(id);
        return byId.map(slabMapper::toDto).orElse(null);
    }
}
