package service;

import controller.dto.PricedSlabAvailabilityDTO;
import controller.dto.SlabDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import mapper.SlabMapper;
import org.eclipse.microprofile.config.ConfigProvider;
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

    final String IMAGE_BASE_URL = ConfigProvider.getConfig().getValue("image.base.url", String.class);

    public List<SlabDTO> getAllProductTypes() {
        List<SlabEntity> all = slabRepository.getAllProductTypes();
        List<SlabDTO> dtos = slabMapper.toDtos(all);

        dtos.forEach(dto -> dto.setImagePath(IMAGE_BASE_URL + dto.getImagePath()));
        return dtos;
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
