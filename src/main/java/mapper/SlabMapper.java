package mapper;

import controller.dto.SlabDTO;
import org.mapstruct.*;
import repository.model.SlabEntity;

import java.util.List;

@Mapper(componentModel = "jakarta")
public interface SlabMapper {

    SlabEntity toEntity(SlabDTO dto, int id);

    /*@Mappings({
            @Mapping(target = "priceId", ignore = true),
    })*/
    SlabDTO toDto(SlabEntity entity);

    List<SlabDTO> toDtos(List<SlabEntity> entityList);

    @Mappings({
        @Mapping(target = "priceId", ignore = true),
    })
    void update(@MappingTarget SlabEntity entity, SlabDTO dto);

}
