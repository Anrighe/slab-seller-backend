package service;

import controller.dto.AuthenticationRequestDTO;
import controller.dto.UserCreationRequestDTO;
import controller.dto.UserInfoUpdateRequestDTO;
import controller.dto.UserPasswordUpdateRequestDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
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
import java.util.Map;
import java.util.HashMap;

/**
 * Service class for handling Keycloak token operations
 */
@Data
@Slf4j
@ApplicationScoped
public class KeycloakService {

    // Configuration parameters
    private String HIGH_LEVEL_USER_USERNAME = ConfigProvider.getConfig().getValue("keycloak.high-level-user-username", String.class);
    private String HIGH_LEVEL_USER_PASSWORD = ConfigProvider.getConfig().getValue("keycloak.high-level-user-password", String.class);

    private String AUTH_SERVER_URL = ConfigProvider.getConfig().getValue("keycloak.auth-server-url", String.class);
    private String REALM = ConfigProvider.getConfig().getValue("keycloak.realm", String.class);

    private String GENERIC_REST_CLIENT_ID = ConfigProvider.getConfig().getValue("keycloak.generic-rest-client.resource", String.class);
    private String GENERIC_REST_CLIENT_SECRET = ConfigProvider.getConfig().getValue("keycloak.generic-rest-client.credentials.secret", String.class);

    private String TOKEN_REST_CLIENT_ID = ConfigProvider.getConfig().getValue("keycloak.token-rest-client.resource", String.class);

    private String TOKEN_REQUEST_ENDPOINT = ConfigProvider.getConfig().getValue("keycloak.token-request-endpoint", String.class);
    private String TOKEN_INTROSPECTION_ENDPOINT = ConfigProvider.getConfig().getValue("keycloak.token-introspection-endpoint", String.class);
    private String USER_CREATION_ENDPOINT = ConfigProvider.getConfig().getValue("keycloak.user-creation-endpoint", String.class);
    private String USER_INFO_UPDATE_ENDPOINT = ConfigProvider.getConfig().getValue("keycloak.user-info-update-endpoint", String.class);
    private String USER_PASSWORD_UPDATE_ENDPOINT_FIRST = ConfigProvider.getConfig().getValue("keycloak.user-password-update-endpoint-first", String.class);
    private String USER_PASSWORD_UPDATE_ENDPOINT_SECOND = ConfigProvider.getConfig().getValue("keycloak.user-password-update-endpoint-second", String.class);

    //TODO: update client use and response return as in updateUserPassword()

    /**
     * Request a token from Keycloak
     *
     * @param authenticationRequestDTO the authentication request
     * @return a Response containing the token
     * @throws Exception if an error occurs during the request
     */
    public Response requestToken(AuthenticationRequestDTO authenticationRequestDTO) throws RuntimeException {
        Form requestBody = new Form()
                .param("grant_type", "password")
                .param("client_id", TOKEN_REST_CLIENT_ID)
                .param("username", authenticationRequestDTO.getUsername())
                .param("password", authenticationRequestDTO.getPassword());

        Client client = ClientBuilder.newClient();

        try {
            return client.target(AUTH_SERVER_URL + REALM + TOKEN_REQUEST_ENDPOINT)
                    .request()
                    .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                    .accept("application/json")
                    .post(Entity.form(requestBody));

        } catch (Exception e) {
            throw new RuntimeException("Failed to obtain token: " + e.getMessage());
        }
    }

    public Response refreshToken(String refreshToken) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        Map<Object, Object> data = new HashMap<>();
        data.put("client_id", TOKEN_REST_CLIENT_ID);
        data.put("grant_type", "refresh_token");
        data.put("refresh_token", refreshToken);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AUTH_SERVER_URL + REALM + TOKEN_REQUEST_ENDPOINT))
                .POST(buildFormDataFromMap(data))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return Response.status(response.statusCode()).entity(response.body()).build();
        } else {
            throw new RuntimeException("Failed to obtain token: Status code: " + response.statusCode());
        }
    }

    /**
     * Validate a token with Keycloak
     *
     * @param token the token to validate
     * @return true if the token is valid, false otherwise
     * @throws Exception if an error occurs during the validation
     */
    public boolean validateToken(String token) throws Exception{
        HttpClient client = HttpClient.newHttpClient();

        Map<Object, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("client_id", GENERIC_REST_CLIENT_ID);
        data.put("client_secret", GENERIC_REST_CLIENT_SECRET);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AUTH_SERVER_URL + REALM + TOKEN_INTROSPECTION_ENDPOINT))
                .POST(buildFormDataFromMap(data))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            JSONObject jsonResponse = new JSONObject(response.body());

            return jsonResponse.get("active") == Boolean.TRUE;
        } else {
            throw new RuntimeException("Failed to verify token: Status code: " + response.statusCode());
        }
    }

    /**
     * Create a user in Keycloak
     *
     * @param userCreationRequestDTO the user creation request
     * @return a Response containing the result of the user creation
     */
    public Response createUser(UserCreationRequestDTO userCreationRequestDTO) throws RuntimeException {

        JSONObject requestBody = new JSONObject()
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
                }});

        Client client = ClientBuilder.newClient();

        try {
            Response response = client.target(AUTH_SERVER_URL + USER_CREATION_ENDPOINT)
                    .request()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + userCreationRequestDTO.getToken())
                    .accept("application/json")
                    .post(Entity.json(requestBody));


            if (response.getStatus() == 201) {
                return Response.status(response.getStatus()).entity(response.getEntity()).build();
            } else {
                log.error(response.getStatus() + " " + response.readEntity(String.class));
                throw new RuntimeException("Failed to create user. Status code: " + response.getStatus());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create user: " + e.getMessage());
        }
    }

    /**
     * Update user information in Keycloak
     *
     * @param userInfoUpdateRequestDTO the user information update request
     * @return a Response containing the result of the user information update
     */
    public Response updateUserInfo(UserInfoUpdateRequestDTO userInfoUpdateRequestDTO) throws RuntimeException {

        JSONObject requestBody = new JSONObject();

        assert userInfoUpdateRequestDTO.getUserId() != null;
        assert userInfoUpdateRequestDTO.getToken() != null;

        if (userInfoUpdateRequestDTO.getEmail() != null)
            requestBody.put("email", userInfoUpdateRequestDTO.getEmail());

        if (userInfoUpdateRequestDTO.getFirstName() != null)
            requestBody.put("firstName", userInfoUpdateRequestDTO.getFirstName());

        if (userInfoUpdateRequestDTO.getLastName() != null)
            requestBody.put("lastName", userInfoUpdateRequestDTO.getLastName());

        Client client = ClientBuilder.newClient();

        try {
            Response response = client.target(AUTH_SERVER_URL + USER_INFO_UPDATE_ENDPOINT + "/" + userInfoUpdateRequestDTO.getUserId())
                    .request()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + userInfoUpdateRequestDTO.getToken())
                    .accept("application/json")
                    .put(Entity.json(requestBody));

            if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
                return Response.status(response.getStatus()).entity(response.getEntity()).build();
            } else {
                log.error(response.getStatus() + " " + response.readEntity(String.class));
                throw new RuntimeException("Failed to create user. Status code: " + response.getStatus());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create user: " + e.getMessage());
        }
    }

    /**
     * Update user password in Keycloak. Only an admin token owner may perform this operation
     *
     * @param userPasswordUpdateRequestDTO the user password update request
     * @return a Response containing the result of the user password update
     */
    public Response updateUserPassword(UserPasswordUpdateRequestDTO userPasswordUpdateRequestDTO) throws RuntimeException {
        JSONObject requestBody = new JSONObject()
                .put("type", "password")
                .put("value", userPasswordUpdateRequestDTO.getNewPassword())
                .put("temporary", false);

        Client client = ClientBuilder.newClient();

        try {
            //TODO: remove user id from the userPasswordUpdateRequestDTO and add a function that gets it automatically
            return client.target(AUTH_SERVER_URL + USER_PASSWORD_UPDATE_ENDPOINT_FIRST + "/"
                            + userPasswordUpdateRequestDTO.getUserId() + USER_PASSWORD_UPDATE_ENDPOINT_SECOND)
                    .request()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + getHighLevelToken())
                    .accept("application/json")
                    .put(Entity.json(requestBody));

        } catch (Exception e) {
            throw new RuntimeException("Failed to create user: " + e.getMessage());
        }
    }

    /**
     * This represents the actual request the user will send for a password update.
     * In order to update its password, the user must know the old password.
     * First a new token request will be made to validate the user. If the user is not valid,
     * the request will be rejected. If the user is valid, the password update request will be made.
     *
     * @param userPasswordUpdateRequestDTO the user password update request
     */
    public Response updateUserPasswordRequest(UserPasswordUpdateRequestDTO userPasswordUpdateRequestDTO) throws RuntimeException {
        try {
            AuthenticationRequestDTO authenticationRequestDTO = new AuthenticationRequestDTO();
            authenticationRequestDTO.setUsername(userPasswordUpdateRequestDTO.getUsername());
            authenticationRequestDTO.setPassword(userPasswordUpdateRequestDTO.getOldPassword());

            Response tokenValidationResponse = requestToken(authenticationRequestDTO);

            if (tokenValidationResponse.getStatus() != 200)
                return tokenValidationResponse;

           return updateUserPassword(userPasswordUpdateRequestDTO);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create user: " + e.getMessage());
        }
    }

    /**
     * Returns a high level token for admin operations.
     * Under no circumstances, this method mustn't be called from rest endpoints.
     *
     * @return the high level token
     */
    private String getHighLevelToken() {
        Form requestBody = new Form()
                .param("grant_type", "password")
                .param("client_id", TOKEN_REST_CLIENT_ID)
                .param("username", HIGH_LEVEL_USER_USERNAME)
                .param("password", HIGH_LEVEL_USER_PASSWORD);

        Client client = ClientBuilder.newClient();

        try {
            Response response = client.target(AUTH_SERVER_URL + REALM + TOKEN_REQUEST_ENDPOINT)
                    .request()
                    .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                    .accept("application/json")
                    .post(Entity.form(requestBody));

            if (response.getStatus() == 200) {
                JSONObject jsonResponse = new JSONObject(response.readEntity(String.class));
                return jsonResponse.get("access_token").toString();
            } else {
                throw new RuntimeException("Failed to obtain token: Status code: " + response.getStatus());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to obtain token: " + e.getMessage());
        }
    }

    //TODO: add getUserId function + endpoint(?)

    /**
     * Build form data from a map
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