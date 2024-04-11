package controller.v1.keycloak;

import controller.dto.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import service.KeycloakService;

/**
 * RESTful web service controller that handles authentication-related requests.
 * It uses the KeycloakTokenService to interact with the Keycloak authentication server.
 */
@Path("/api/authentication")
@Slf4j
public class AuthenticationResource {
    @Inject
    KeycloakService keycloakService;

    /**
     * Handles POST requests to /token/request.
     * It takes an AuthenticationRequestDTO object as input and returns a Response.
     * The method uses the KeycloakTokenService to request a token from Keycloak.
     * If the request is successful, it returns a 200 OK response with the token.
     * If the request fails, it returns a 401 Unauthorized response.
     */
    @POST
    @Path("/token/request")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response requestToken(AuthenticationRequestDTO authenticationRequestDTO) {

        try {
            Response serviceResponse = keycloakService.requestToken(authenticationRequestDTO);

            if (serviceResponse.getStatus() != 200)
                return serviceResponse;

            return Response.ok().entity(new AuthenticationResponseDTO(serviceResponse)).type(MediaType.APPLICATION_JSON).build();

        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Handles POST requests to /token/refresh.
     * It takes a TokenRefreshRequestDTO object as input and returns a Response.
     * The method uses the KeycloakTokenService to refresh a token.
     * If the request is successful, it returns a 200 OK response with the new token.
     * If the request fails, it returns a 401 Unauthorized response.
     */
    @POST
    @Path("/token/refresh")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response refreshToken(TokenRefreshRequestDTO tokenRefreshRequestDTO) {

        Response response;
        TokenRefreshResponseDTO tokenRefreshResponse = new TokenRefreshResponseDTO();

        try {
            response = keycloakService.refreshToken(tokenRefreshRequestDTO.getRefreshToken());

            JSONObject jsonResponse = new JSONObject(response.getEntity().toString());
            tokenRefreshResponse.setNewToken(jsonResponse.getString("access_token"));

            return Response.ok().entity(tokenRefreshResponse).type(MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    /**
     * Handles POST requests to /token/validate.
     * It takes a TokenValidationRequestDTO object as input and returns a Response.
     * The method uses the KeycloakTokenService to validate a token.
     * If the token is valid, it returns a 200 OK response.
     * If the token is not valid, it returns a 401 Unauthorized response.
     */
    @POST
    @Path("/token/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response validateToken(TokenValidationRequestDTO request) {

        boolean isValid;
        TokenValidationResponseDTO tokenValidationResponse = new TokenValidationResponseDTO();

        try {
            isValid = keycloakService.validateToken(request.getToken());
            tokenValidationResponse.setTokenValid(isValid);
            log.debug("Token valid: " + tokenValidationResponse.isTokenValid());
            return Response.ok().entity(tokenValidationResponse).type(MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }














}
