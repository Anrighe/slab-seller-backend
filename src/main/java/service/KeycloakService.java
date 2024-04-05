package service;

import controller.dto.UserCreationRequestDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Service class for handling Keycloak token operations.
 */
@Data
@Slf4j
@ApplicationScoped
public class KeycloakService {

    String authServerUrl = ConfigProvider.getConfig().getValue("keycloak.auth-server-url", String.class);
    String realm = ConfigProvider.getConfig().getValue("keycloak.realm", String.class);

    String genericRestClientId = ConfigProvider.getConfig().getValue("keycloak.generic-rest-client.resource", String.class);
    String genericRestClientSecret = ConfigProvider.getConfig().getValue("keycloak.generic-rest-client.credentials.secret", String.class);

    String tokenRestClientId = ConfigProvider.getConfig().getValue("keycloak.token-rest-client.resource", String.class);

    String tokenRequestEndpoint = ConfigProvider.getConfig().getValue("keycloak.token-request-endpoint", String.class);
    String userCreationEndpoint = ConfigProvider.getConfig().getValue("keycloak.user-creation-endpoint", String.class);

    /**
     * Request a token from Keycloak.
     *
     * @param username the username
     * @param password the password
     * @return a Response containing the token
     * @throws Exception if an error occurs during the request
     */
    public Response requestToken(String username, String password) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        Map<Object, Object> data = new HashMap<>();
        data.put("grant_type", "password");
        data.put("client_id", tokenRestClientId);
        //data.put("client_secret", genericRestClientSecret);
        data.put("username", username);
        data.put("password", password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(authServerUrl + realm + tokenRequestEndpoint))
                .POST(buildFormDataFromMap(data))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return Response.status(response.statusCode()).entity(response.body()).build();
        } else {
            throw new RuntimeException("Failed to obtain token. Status code: " + response.statusCode());
        }
    }

    public Response refreshToken(String refreshToken) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        Map<Object, Object> data = new HashMap<>();
        data.put("client_id", tokenRestClientId);
        data.put("grant_type", "refresh_token");
        data.put("refresh_token", refreshToken);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(authServerUrl + realm + tokenRequestEndpoint))
                .POST(buildFormDataFromMap(data))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return Response.status(response.statusCode()).entity(response.body()).build();
        } else {
            throw new RuntimeException("Failed to obtain token. Status code: " + response.statusCode());
        }
    }

    /**
     * Validate a token with Keycloak.
     *
     * @param token the token to validate
     * @return true if the token is valid, false otherwise
     * @throws Exception if an error occurs during the validation
     */
    public boolean validateToken(String token) throws Exception{
        HttpClient client = HttpClient.newHttpClient();

        Map<Object, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("client_id", genericRestClientId);
        data.put("client_secret", genericRestClientSecret);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(authServerUrl + realm + "/protocol/openid-connect/token/introspect"))
                .POST(buildFormDataFromMap(data))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            JSONObject jsonResponse = new JSONObject(response.body());

            return jsonResponse.get("active") == Boolean.TRUE;
        } else {
            throw new RuntimeException("Failed to verify token. Status code: " + response.statusCode());
        }
    }

    /**
     * Create a user in Keycloak.
     *
     * @param userCreationRequestDTO the user creation request
     * @return a Response containing the result of the user creation
     */
    public Response createUser(UserCreationRequestDTO userCreationRequestDTO) {

        var requestBody = new JSONObject()
                .put("username", userCreationRequestDTO.getUsername())
                .put("enabled", userCreationRequestDTO.isEnabled())
                .put("email", userCreationRequestDTO.getEmail())
                .put("firstName", userCreationRequestDTO.getFirstName())
                .put("lastName", userCreationRequestDTO.getLastName())
                .put("credentials", new ArrayList<>() {{
                    add(new JSONObject()
                            .put("type", "password")
                            .put("value", userCreationRequestDTO.getPassword())
                            .put("temporary", false));
                }}).toString();

        Client client = ClientBuilder.newClient();

        try {
            Response response = client.target(authServerUrl + userCreationEndpoint)
                    .request()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + userCreationRequestDTO.getToken())
                    .accept("application/json")
                    .post(jakarta.ws.rs.client.Entity.json(requestBody));


            if (response.getStatus() == 201) {
                return Response.status(response.getStatus()).entity(response.getEntity()).build();
            } else {
                log.error(response.getStatus() + " " + response.readEntity(String.class));
                throw new RuntimeException("Failed to create user. Status code: " + response.getStatus());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create user. " + e.getMessage());
        }
    }

    /**
     * Build form data from a map.
     *
     * @param data the map containing the form data
     * @return a BodyPublisher containing the form data
     */
    private static HttpRequest.BodyPublisher buildFormDataFromMap(Map<Object, Object> data) {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (!builder.isEmpty()) {
                builder.append("&");
            }
            builder.append(entry.getKey());
            builder.append("=");
            builder.append(entry.getValue());
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }
}