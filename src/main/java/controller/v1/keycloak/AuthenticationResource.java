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

        Response response;
        try {
            response = keycloakTokenService.requestToken(request.getUsername(), request.getPassword());
            log.debug("Token: " + response.getEntity().toString());
            return Response.ok().entity(response.getEntity()).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    @POST
    @Path("/token/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response validateToken(ValidationRequestDTO request) {

        JSONObject jsonResponse = new JSONObject();
        boolean isValid;
        try {
            isValid = keycloakTokenService.validateToken(request.getToken());
            jsonResponse.put("active", isValid);
            log.debug("Token status: " + jsonResponse.get("active"));
            return Response.ok().entity(jsonResponse.toString()).type(MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

}
