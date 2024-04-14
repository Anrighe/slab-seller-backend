package controller.dto;

import jakarta.ws.rs.core.Response;
import lombok.Data;
import org.json.JSONObject;

@Data
public class TokenValidationResponseDTO {
    private boolean tokenValid;

    public TokenValidationResponseDTO(Response response) {
        JSONObject jsonResponse = new JSONObject(response.readEntity(String.class));
        this.tokenValid = jsonResponse.getBoolean("active");
    }
}
