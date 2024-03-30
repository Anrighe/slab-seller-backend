package controller.v1.keycloak;

import controller.dto.AuthenticationRequestDTO;
import controller.dto.ValidationRequestDTO;
import jakarta.inject.Inject;
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
    @Inject
    KeycloakTokenService keycloakTokenService;

    @POST
    @Path("/token")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response requestToken(AuthenticationRequestDTO request) {
        keycloakTokenService = new KeycloakTokenService();

        String response;
        try {
            response = keycloakTokenService.requestToken(request.getUsername(), request.getPassword());
            JSONObject jsonResponse = new JSONObject(response);
            log.debug("Token: " + jsonResponse.getString("access_token"));
            return Response.ok().entity(response).build();

        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    @POST
    @Path("/token/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response validateToken(ValidationRequestDTO request) {

        String response;
        try {
            response = keycloakTokenService.validateToken(request.getToken());
            JSONObject jsonResponse = new JSONObject(response);
            log.debug("Token validity: " + jsonResponse.get("active"));
            return Response.ok().entity(response).build();

        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

}
