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
import org.apache.camel.util.Pair;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.json.JSONObject;
import repository.model.UserEntity;
import service.EmailService;
import service.KeycloakService;
import service.PasswordService;

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

            UserEntity user = keycloakService.getUserByValue(new Pair<>("email", passwordRecoveryRequestDTO.getEmail()));

            // If the user does not exist, or it's disabled, return 200: no mail will be sent
            if (user.getId() == null || user.getUsername() == null || user.getEmail() == null || !user.isEnabled()) {
                return Response.status(Response.Status.OK).build();
            }

            String generatedPassword = PasswordService.generatePassword();
            //TODO: Impostare la nuova password TEMPORANEA all'utente

            Map<String, String> personalizationData = Map.of(
                    "username", user.getUsername(),
                    "password", generatedPassword
            );

            // Response emailServiceResponse = emailService.sendEmail(passwordRecoveryRequestDTO, personalizationData);

            try (Response emailServiceResponse = emailService.sendEmail(passwordRecoveryRequestDTO, personalizationData)) {
                if (emailServiceResponse.getStatus() == Response.Status.ACCEPTED.getStatusCode()) {
                    return Response.status(Response.Status.OK).build();
                } else {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }
            }

            //TODO: loggare su database l'invio della mail di ripristino password



        } catch (WebApplicationException e) {
            log.error("Error during password recovery procedure, token is unauthorized: {}", e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (RuntimeException e) {
            log.error("Runtime exception during password recovery procedure: {}", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Generic exception during password recovery procedure: {}", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
