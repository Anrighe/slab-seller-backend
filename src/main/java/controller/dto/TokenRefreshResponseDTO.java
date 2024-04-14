package controller.dto;

import jakarta.ws.rs.core.Response;
import lombok.Data;
import org.json.JSONObject;

@Data
public class TokenRefreshResponseDTO {
    private String newToken;
    private String newRefreshToken;
    private String tokenType;

    public TokenRefreshResponseDTO(Response response) {
        JSONObject jsonResponse = new JSONObject(response.readEntity(String.class));
        this.newToken = jsonResponse.get("access_token").toString();
        this.newRefreshToken = jsonResponse.get("refresh_token").toString();
        this.tokenType = jsonResponse.get("token_type").toString();
    }
}
