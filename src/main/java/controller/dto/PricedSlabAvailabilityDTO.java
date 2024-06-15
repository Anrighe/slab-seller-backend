package controller.dto;

import lombok.Data;

@Data
public class PricedSlabAvailabilityDTO {
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
