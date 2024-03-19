package controller.v1;

import controller.dto.SlabDTO;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
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
@Path("/slab")
public class SlabResource {

    @Inject
    SlabService slabService;

    @GET
    @Path("/all")
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
                    description = "List successfully returned",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = SchemaType.ARRAY, implementation = SlabDTO.class)))})
    @Operation(summary = "Returns a list of all slabs", description = "Returns a list of all slabs")
    public List<SlabDTO> getAll() {
        try {
            return slabService.getAll();
        } catch (Throwable e) {
            log.error("Error: ", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

}
