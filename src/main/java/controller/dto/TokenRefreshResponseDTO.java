package controller.dto;

import lombok.Data;

@Data
public class TokenRefreshResponseDTO {
    private String newToken;
}
