package controller.dto;

import jakarta.ws.rs.core.Response;
import lombok.Data;
import org.json.JSONObject;

@Data
public class AuthenticationResponseDTO {
    private String token;
    private String refreshToken;

    public AuthenticationResponseDTO(Response response) {
        JSONObject jsonResponse = new JSONObject(response.readEntity(String.class));
        this.token = jsonResponse.get("access_token").toString();
        this.refreshToken = jsonResponse.get("refresh_token").toString();
    }
}
