package controller.v1.email;


import jakarta.ws.rs.WebApplicationException;
import controller.dto.PasswordRecoveryRequestDTO;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import service.EmailService;
import service.KeycloakService;

import java.util.Map;

@Path("/api/v1/email")
@Slf4j
public class EmailResource {

    @Inject
    EmailService emailService;
    @Inject
    KeycloakService keycloakService;

    @POST
    @Path("/send")
    @Consumes(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "400",
                    description = "Bad Request"),
            @APIResponse(
                    responseCode = "401",
                    description = "Unauthorized"),
            @APIResponse(
                    responseCode = "403",
                    description = "Forbidden"),
            @APIResponse(
                    responseCode = "404",
                    description = "Not found"),
            @APIResponse(
                    responseCode = "500",
                    description = "Internal Server Error"),
            @APIResponse(
                    responseCode = "200",
                    description = "Email sent successfully")
    })
    public Response requestPasswordRecoveryEmail(@Context HttpHeaders headers, PasswordRecoveryRequestDTO passwordRecoveryRequestDTO) {
        try {
            // Request token validation
            String authorization = headers.getHeaderString("Authorization");
            log.info("User authorization: {}", authorization);

            //TODO: Re-enable token authentication
            /*TokenValidationResponseDTO tokenValidationResponseDTO = keycloakService.validateTokenAndGetResponse(authorization);
            if (!tokenValidationResponseDTO.isTokenValid()) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }*/

            //TODO: Check se la mail esiste tra gli utenti (richiesta a keycloak?)
            String username = "Idiota";

            //TODO: Creare una nuova password
            //TODO: Impostare la nuova password TEMPORANEA all'utente
            String generatedPassword = "12345";

            Map<String, String> personalizationData = Map.of(
                    "username", username,
                    "password", generatedPassword
            );

            Response emailServiceResponse = emailService.sendEmail(passwordRecoveryRequestDTO, personalizationData);

            if (emailServiceResponse.getStatus() == Response.Status.ACCEPTED.getStatusCode()) {
                return Response.status(Response.Status.OK).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

        } catch (WebApplicationException e) {
            log.error("Error sending email, token is unauthorized: {}", e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Generic exception while sending email: {}", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
