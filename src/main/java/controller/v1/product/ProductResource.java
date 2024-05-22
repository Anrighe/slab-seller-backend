package controller.v1.product;

import controller.dto.SlabDTO;
import controller.dto.TokenValidationResponseDTO;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import service.KeycloakService;
import service.SlabService;

@Slf4j
@Path("/product")
public class ProductResource {

    @Inject
    SlabService slabService;

    @Inject
    KeycloakService keycloakService;

    @GET
    @Path("/fetch")
    @Produces(MediaType.APPLICATION_JSON)
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
                    description = "List of products successfully returned",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = SchemaType.ARRAY, implementation = SlabDTO.class)))})
    @Operation(summary = "Returns a list of available slabs",
            description = "Returns all the available slabs. " +
                    "The customer client must prove it was able to successfully login by validating its token." +
                    "The header of the request must include a parameter \"Authorization\" with the following value type: \"Bearer JWT_TOKEN\"")
    public Response fetchProducts(@HeaderParam("Authorization") String authorization) {
        try {

            log.debug("Authorization header: {}", authorization);

            if (authorization == null || !authorization.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header");
                throw new WebApplicationException("Unauthorized", Response.Status.UNAUTHORIZED);
            }

            Response validateTokenResponse = keycloakService.validateToken(authorization.substring("Bearer ".length()).trim());

            TokenValidationResponseDTO tokenValidationResponseDTO = new TokenValidationResponseDTO(validateTokenResponse);
            log.debug("Token validation result: {}", tokenValidationResponseDTO.isTokenValid());

            if (validateTokenResponse.getStatus() == 200 && tokenValidationResponseDTO.isTokenValid()) {
                return Response.ok().entity(slabService.getAll()).type(MediaType.APPLICATION_JSON).build();
            } else {
                log.warn("Token validation failed or token is invalid");
                throw new WebApplicationException("Unauthorized", Response.Status.UNAUTHORIZED);
            }
        } catch (WebApplicationException e) {
            log.error("WebApplicationException in fetchProducts: {}", e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Error in fetchProducts: ", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }


}



