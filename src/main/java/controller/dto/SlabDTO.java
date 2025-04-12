package controller.dto;

import lombok.Data;

@Data
public class SlabDTO {
    private String id;
    private String collection;
    private String color;
    private String tone;
    private int width;
    private int height;
    private short priceId;
    private String imagePath;
}
