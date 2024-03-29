package controller.v1.keycloak;

import controller.dto.AuthenticationRequestDTO;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import service.KeycloakTokenService;

@Path("/authentication")
@Slf4j
public class AuthenticationResource {

    @POST
    @Path("/token")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response requestToken(AuthenticationRequestDTO request) {
        KeycloakTokenService keycloakTokenService = new KeycloakTokenService();

        String token;
        try {
            token = keycloakTokenService.requestToken(request.getUsername(), request.getPassword());
            JSONObject jsonObject = new JSONObject(token);
            log.debug("Token: " + jsonObject.getString("access_token"));
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        return Response.ok().entity(token).build();
    }
}
