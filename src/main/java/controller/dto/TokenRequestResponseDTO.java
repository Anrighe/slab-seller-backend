package controller.dto;

import lombok.Data;

@Data
public class TokenRequestResponseDTO {
    private String newToken;
    private String refreshToken;
}
