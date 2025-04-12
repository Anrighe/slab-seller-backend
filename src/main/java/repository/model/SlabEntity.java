package repository.model;

import lombok.*;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

@Data
public class SlabEntity {
    private String id;
    private String collection;
    private String color;
    private String tone;
    private int width;
    private int height;
    private int priceId;
    private String imagePath;
}
