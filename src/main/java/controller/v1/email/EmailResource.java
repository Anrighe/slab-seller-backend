package controller.v1.email;


import jakarta.ws.rs.WebApplicationException;
import controller.dto.PasswordRecoveryRequestDTO;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.util.Pair;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import repository.PasswordRecoveryRequestRepository;
import repository.model.PasswordRecoveryRequestEntity;
import repository.model.UserEntity;
import service.EmailService;
import service.KeycloakService;
import utils.HashUtil;
import utils.PasswordRecoveryRequestUtil;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Path("/api/v1/email")
@Slf4j
public class EmailResource {

    private final int PASSWORD_RECOVERY_REQUEST_TIMEOUT_SECONDS = ConfigProvider.getConfig().getValue("mailsender.password-recovery-request-timeout-seconds", Integer.class);

    private final String HTTP_FRONT_END_CORS_ORIGIN = ConfigProvider.getConfig().getValue("quarkus.http.cors.origins", String.class);
    private final String FRONT_END_RESET_PASSWORD_ROUTE = "/passwordrecovery/reset";

    @Inject
    EmailService emailService;
    @Inject
    KeycloakService keycloakService;
    @Inject
    PasswordRecoveryRequestRepository passwordRecoveryRequestRepository;

    @POST
    @Path("/passwordrecovery")
    @Consumes(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "400",
                    description = "Bad Request"),
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
                    description = "Password recovery request completed")
    })
    @Operation(summary = "Requests the process for the password recovery",
            description = "Logs the requests and if eligible (user exists and it's active, hasn't recently sent any other requests)" +
                    " sends the password recovery email to the specified user." +
                    " Completing the procedure with a 200 return code, does not guarantee an email has been actually sent.")
    public Response requestPasswordRecoveryEmail(PasswordRecoveryRequestDTO passwordRecoveryRequestDTO) {
        try {

            UserEntity user = keycloakService.getUserByValue(new Pair<>("email", passwordRecoveryRequestDTO.getEmail()));

            // If the user does not exist, or it's disabled, return 200: no mail will be sent
            if (user.getId() == null || user.getUsername() == null || user.getEmail() == null || !user.isEnabled()) {
                log.error("Password recovery request could not be completed: could not find user {}", user.getEmail());
                return Response.status(Response.Status.OK).build();
            }

            String generatedHash = HashUtil.generateHashedUrlFriendlyId(user.getEmail(), Instant.now());

            PasswordRecoveryRequestEntity passwordRecoveryRequestEntity = new PasswordRecoveryRequestEntity(
                    user.getEmail(),
                    generatedHash
            );

            // Checking if the user can send a password recovery request
            List<PasswordRecoveryRequestEntity> passwordRecoveryRequestForEmail =
                    passwordRecoveryRequestRepository.getAllPasswordRecoveryRequestForEmail(passwordRecoveryRequestEntity);

            // Filters valid password recovery requests: these do not take into consideration the timeout for each request it can be sent to an email address
            List<PasswordRecoveryRequestEntity> validPasswordRecoveryRequestForEmail =
                    PasswordRecoveryRequestUtil.filterValidPasswordRecoveryRequests(passwordRecoveryRequestForEmail);

            // Responds with OK if not enough time is passed since previous request for the same email address
            if (PasswordRecoveryRequestUtil.isAnyPasswordRecoveryRequestInTimeout(validPasswordRecoveryRequestForEmail, PASSWORD_RECOVERY_REQUEST_TIMEOUT_SECONDS)) {
                log.info("Password recovery request for user {} could not complete due to unexpired previous requests", user.getEmail());
                return Response.status(Response.Status.OK).build();
            }

            // Invalidate all the other password recovery requests for the user
            if (passwordRecoveryRequestRepository.invalidatePasswordRecoveryRequestsForEmail(passwordRecoveryRequestEntity)) {
                log.error("Invalidated all other password recovery requests for user {}", user.getUsername());
            }

            // Insert the new password recovery request
            if (!passwordRecoveryRequestRepository.insertPasswordRecoveryRequest(passwordRecoveryRequestEntity)) {
                log.error("Error while inserting password recovery request for user {}", user.getUsername());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            // Data used to populate the email template
            Map<String, String> personalizationData = Map.of(
                    "username", user.getUsername(),
                    "resetlink", String.format("%s%s/%s", HTTP_FRONT_END_CORS_ORIGIN, FRONT_END_RESET_PASSWORD_ROUTE, generatedHash)
            );

            try (Response emailServiceResponse = emailService.sendEmail(passwordRecoveryRequestDTO, personalizationData)) {
                if (emailServiceResponse.getStatus() == Response.Status.ACCEPTED.getStatusCode()) {
                    return Response.status(Response.Status.OK).build();
                } else {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }
            }

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
