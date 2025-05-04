package controller.v1.keycloak;

import controller.dto.UserCreationRequestDTO;
import controller.dto.UserInfoUpdateRequestDTO;
import controller.dto.UserPasswordUpdateRequestDTO;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import repository.PasswordRecoveryRequestRepository;
import repository.model.PasswordRecoveryRequestEntity;
import service.KeycloakService;

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
                    responseCode = "500",
                    description = "Internal Server Error"),
            @APIResponse(
                    responseCode = "201",
                    description = "User created successfully")
    })
    public Response createUser(UserCreationRequestDTO request) {
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
    @Path("/verify/passwordrecovery")
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

            log.info("Password recovery request with hashed id {} is valid", passwordRecoveryRequestHashedId);
            return Response.status(Response.Status.OK).build();

        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
