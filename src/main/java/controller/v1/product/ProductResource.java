package controller.v1.product;

import controller.dto.TokenValidationResponseDTO;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import service.KeycloakService;
import controller.dto.SlabDTO;
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
import service.SlabService;

import java.util.List;

@Slf4j
@Path("api/v1/products")
public class ProductResource {

    @Inject
    SlabService slabService;

    @Inject
    KeycloakService keycloakService;

    @GET
    @Path("/type")
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
    @Operation(summary = "Returns a list of all types of slabs",
            description = "Fetches all types of slabs (note: does not return availability, for that use fetchProductsTypeAndAvailability). " +
                    "The customer client must prove it was able to successfully login by validating its token." +
                    "The header of the request must include a parameter \"Authorization\" with the following value type: \"Bearer JWT_TOKEN\"")
    public Response fetchProductsType(
            @Context HttpHeaders headers,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("offset") @DefaultValue("0") int offset) {

        log.info("Fetching products type from the fetchProductsType");

        String authorization = headers.getHeaderString("Authorization");
        log.info("User authorization: {}", authorization);

        try {
            // Token validation
            TokenValidationResponseDTO tokenValidationResponseDTO = keycloakService.validateTokenAndGetResponse(authorization);
            if (!tokenValidationResponseDTO.isTokenValid()) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            List<SlabDTO> slabs = slabService.getProductTypesPaged(limit, offset);

            return Response.ok().entity(slabs).type(MediaType.APPLICATION_JSON).build();
        } catch (WebApplicationException e) {
            log.error("WebApplicationException in fetchProducts: {}", e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Error in fetchProducts: ", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/availability")
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
    @Operation(summary = "Returns all the available slabs",
            description = "Returns a list containing all available slabs (note: for product types only fetchProductsType will be faster) " +
                    "The customer client must prove it was able to successfully login by validating its token." +
                    "The header of the request must include a parameter \"Authorization\" with the following value type: \"Bearer JWT_TOKEN\"")
    public Response fetchProductsTypeAndAvailability(@Context HttpHeaders headers) {
        log.info("Fetching products type from the fetchProductsTypeAndAvailability");

        String authorization = headers.getHeaderString("Authorization");
        log.info("User authorization: {}", authorization);

        try {
            TokenValidationResponseDTO tokenValidationResponseDTO = keycloakService.validateTokenAndGetResponse(authorization);

            if (!tokenValidationResponseDTO.isTokenValid()) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            return Response.ok().entity(slabService.getAllProductsWithDetailsAndAvailability()).type(MediaType.APPLICATION_JSON).build();
        } catch (WebApplicationException e) {
            log.error("WebApplicationException in fetchProducts: {}", e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Error in fetchProducts: ", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}



