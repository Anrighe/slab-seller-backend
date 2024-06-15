package Utils;

import controller.dto.TokenValidationResponseDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import service.KeycloakService;

@Slf4j
@ApplicationScoped
public class ProductResourceUtils {
    @Inject
    KeycloakService keycloakService;

    /**
     * Validates the authorization token and returns the token validation response DTO.
     *
     * @param authorizationHeader Authorization header with Bearer JWT_TOKEN.
     * @return TokenValidationResponseDTO containing the result of token validation.
     * @throws WebApplicationException if the authorization header is missing or invalid,
     *                                 or if token validation fails.
     */
    public TokenValidationResponseDTO validateTokenAndGetResponse(String authorizationHeader) {
        log.debug("Authorization header: {}", authorizationHeader);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header");
            throw new WebApplicationException("Unauthorized", Response.Status.UNAUTHORIZED);
        }

        Response validateTokenResponse = keycloakService.validateToken(authorizationHeader.substring("Bearer ".length()).trim());

        TokenValidationResponseDTO tokenValidationResponseDTO = new TokenValidationResponseDTO(validateTokenResponse);
        log.debug("Token validation result: {}", tokenValidationResponseDTO.isTokenValid());

        if (validateTokenResponse.getStatus() != 200 || !tokenValidationResponseDTO.isTokenValid()) {
            log.warn("Token validation failed or token is invalid");
            throw new WebApplicationException("Unauthorized", Response.Status.UNAUTHORIZED);
        }

        return tokenValidationResponseDTO;
    }
}
