package controller.v1.keycloak;

import controller.dto.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.jboss.resteasy.reactive.RestPath;
import repository.PasswordRecoveryRequestRepository;
import repository.model.PasswordRecoveryRequestEntity;
import service.KeycloakService;

import java.util.Objects;

@Path("/api/v1/user")
@Slf4j
public class UserResource {

    @Inject
    KeycloakService keycloakService;
    @Inject
    PasswordRecoveryRequestRepository passwordRecoveryRequestRepository;

    @POST
    @Path("/create")
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
                    responseCode = "409",
                    description = "User with same email or username already exists"),
            @APIResponse(
                    responseCode = "500",
                    description = "Internal Server Error"),
            @APIResponse(
                    responseCode = "201",
                    description = "User created successfully"),

    })
    @Operation(summary = "Sends a request for a user creation to the keycloak service")
    public Response createUser(UserCreationRequestDTO request) {

        if (request.getUsername() == null || request.getUsername().isEmpty() ||
                request.getPassword() == null || request.getPassword().isEmpty() ||
                request.getEmail() == null || request.getEmail().isEmpty() ||
                request.getFirstName() == null || request.getFirstName().isEmpty() ||
                request.getLastName() == null || request.getLastName().isEmpty()) {

            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        try {
            return keycloakService.createUser(request);

        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT
    @Path("/update/info")
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
                    responseCode = "204",
                    description = "User info successfully updated")
    })
    public Response updateUserInfo(UserInfoUpdateRequestDTO request) {
        try {
            return keycloakService.updateUserInfo(request);

        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT
    @Path("/update/password")
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
                    responseCode = "204",
                    description = "User password successfully updated")
    })
    public Response updateUserPassword(UserPasswordUpdateRequestDTO request) {
        try {
            return keycloakService.updateUserPasswordRequest(request);

        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/passwordrecovery/verify")
    @Consumes(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "400",
                    description = "Bad Request"),
            @APIResponse(
                    responseCode = "401",
                    description = "Password recovery request does not exists, disabled, already used or expired"),
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
                    description = "Valid password recovery request")
    })
    @Operation(summary = "Validates the password recovery requests by checking if it exists and if has not been used or invalidated")
    public Response validatePasswordRecoveryRequest(final String passwordRecoveryRequestHashedId) {
        try {
            PasswordRecoveryRequestEntity passwordRecoveryRequestEntity = passwordRecoveryRequestRepository.getPasswordRecoveryRequestByHashedId(passwordRecoveryRequestHashedId);

            if (passwordRecoveryRequestEntity == null || !passwordRecoveryRequestEntity.isValid()) {
                log.warn("Unauthorized password recovery request with hashed id {}", passwordRecoveryRequestHashedId);
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            log.info("Password recovery request for user {} with hash id {} is valid", passwordRecoveryRequestEntity.getEmail(), passwordRecoveryRequestHashedId);
            return Response.status(Response.Status.OK).build();

        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/passwordrecovery/email/{requestHash}")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "400",
                    description = "Bad Request"),
            @APIResponse(
                    responseCode = "401",
                    description = "Password recovery request does not exists, disabled, already used or expired"),
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
                    description = "Valid password recovery request")
    })
    @Operation(summary = "Retrieves the email associated to the password recovery request, if the hash request id and the request itself are valid")
    public Response getPasswordRecoveryRequestEmail(@RestPath final String requestHash) {

        if (requestHash == null || requestHash.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        try {
            PasswordRecoveryRequestEntity passwordRecoveryRequestEntity = passwordRecoveryRequestRepository.getPasswordRecoveryRequestByHashedId(requestHash);

            if (passwordRecoveryRequestEntity == null || !passwordRecoveryRequestEntity.isValid()) {
                log.warn("Unauthorized password recovery request with hashed id {}", requestHash);
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            log.info("Password recovery request for user {} with hash id {} is valid", passwordRecoveryRequestEntity.getEmail(), requestHash);
            return Response.status(Response.Status.OK).entity(new PasswordRecoveryEmailResponseDTO(passwordRecoveryRequestEntity.getEmail())).build();

        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/passwordrecovery/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "400",
                    description = "Bad Request"),
            @APIResponse(
                    responseCode = "401",
                    description = "Password recovery request does not exists, disabled, already used or expired"),
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
                    description = "Password successfully set")
    })
    @Operation(summary = "Requests the password update in the password recovery procedure")
    public Response requestPasswordRecoveryUpdate(final PasswordRecoveryUpdateRequestDTO passwordRecoveryUpdateRequestDTO) {

        if (passwordRecoveryUpdateRequestDTO == null
                || passwordRecoveryUpdateRequestDTO.getPasswordRecoveryRequestHashedId() == null
                || passwordRecoveryUpdateRequestDTO.getNewPassword() == null
                || passwordRecoveryUpdateRequestDTO.getConfirmPassword() == null
                || passwordRecoveryUpdateRequestDTO.getPasswordRecoveryEmail() == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        // If specified passwords do not match
        if (!Objects.equals(passwordRecoveryUpdateRequestDTO.getNewPassword(), passwordRecoveryUpdateRequestDTO.getConfirmPassword())) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        try {
            PasswordRecoveryRequestEntity passwordRecoveryRequestEntity = passwordRecoveryRequestRepository
                    .getPasswordRecoveryRequestByHashedId(passwordRecoveryUpdateRequestDTO.getPasswordRecoveryRequestHashedId());

            if (passwordRecoveryRequestEntity == null || !passwordRecoveryRequestEntity.isValid()) {
                log.warn("Unauthorized password recovery request with hashed id {}", passwordRecoveryUpdateRequestDTO.getPasswordRecoveryRequestHashedId());
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            log.info("Password recovery request for user {} with hash id {} is valid", passwordRecoveryRequestEntity.getEmail(), passwordRecoveryUpdateRequestDTO.getPasswordRecoveryRequestHashedId());

            String keycloakUserId = keycloakService.getUserIdFromEmail(passwordRecoveryRequestEntity.getEmail());

            Response keycloakResponse = keycloakService.updateUserPassword(
                    passwordRecoveryUpdateRequestDTO.getPasswordRecoveryEmail(),
                    passwordRecoveryUpdateRequestDTO.getNewPassword(),
                    keycloakUserId
            );

            if (keycloakResponse.getStatus() == Response.Status.OK.getStatusCode() || keycloakResponse.getStatus() == 204) {

                // Invalidate all the other password recovery requests for the user
                if (passwordRecoveryRequestRepository.invalidatePasswordRecoveryRequestsForEmail(passwordRecoveryRequestEntity)) {
                    log.info("Invalidated all other password recovery requests for user {} with email {}", keycloakUserId, passwordRecoveryRequestEntity.getEmail());
                } else {
                    log.error("Could not invalidate all other password recovery requests for user {} with email {}", keycloakUserId, passwordRecoveryRequestEntity.getEmail());
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            return Response.status(Response.Status.OK).build();

        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}
