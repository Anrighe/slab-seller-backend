package service;

import controller.dto.PricedSlabAvailabilityDTO;
import controller.dto.SlabDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import mapper.SlabMapper;
import repository.SlabRepository;
import repository.model.PricedSlabAvailabilityEntity;
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

    public List<SlabDTO> getAllProductTypes() {
        List<SlabEntity> all = slabRepository.getAllProductTypes();
        return slabMapper.toDtos(all);
    }

    public SlabDTO getProductTypeById(String id) {
        Optional<SlabEntity> byId = slabRepository.getProductTypeById(id);
        return byId.map(slabMapper::toDto).orElse(null);
    }

    public List<PricedSlabAvailabilityDTO> getAllProductsWithDetailsAndAvailability() {
        List<PricedSlabAvailabilityEntity> all = slabRepository.getAllProductsWithDetailsAndAvailability();
        return slabMapper.toPricedSlabAvailabilityDtos(all);
    }
}
