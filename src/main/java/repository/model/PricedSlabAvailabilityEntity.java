package repository.model;

import lombok.Data;

/**
 * Model for the
 */
@Data
public class PricedSlabAvailabilityEntity {
    private String id;
    private String collection;
    private String color;
    private String tone;
    private int width;
    private int height;
    private Double price;
    private String currency;
    private int quantity;
}
