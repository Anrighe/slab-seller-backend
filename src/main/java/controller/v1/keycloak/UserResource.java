package controller.v1.keycloak;

import controller.dto.UserCreationRequestDTO;
import controller.dto.UserInfoUpdateRequestDTO;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import service.KeycloakService;

@Path("/api/user")
@Slf4j
public class UserResource {

    @Inject
    KeycloakService keycloakService;

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
                    description = "User info updated successfully")
    })
    public Response updateUserInfo(UserInfoUpdateRequestDTO request) {
        try {
            return keycloakService.updateUserInfo(request);

        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
