package controller.v1.product;

import utils.ProductResourceUtils;
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

@Slf4j
@Path("/products")
public class ProductResource {

    @Inject
    SlabService slabService;

    @Inject
    ProductResourceUtils productResourceUtils;

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
    public Response fetchProductsType(@HeaderParam("Authorization") String authorization) {
        try {
            productResourceUtils.validateTokenAndGetResponse(authorization);
            return Response.ok().entity(slabService.getAllProductTypes()).type(MediaType.APPLICATION_JSON).build();
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
    public Response fetchProductsTypeAndAvailability(@HeaderParam("Authorization") String authorization) {
        try {
            productResourceUtils.validateTokenAndGetResponse(authorization);
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



