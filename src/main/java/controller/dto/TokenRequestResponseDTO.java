package controller.dto;

import lombok.Data;

@Data
public class TokenRequestResponseDTO {
    String newToken;
    String refreshToken;
}
