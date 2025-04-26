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
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Instant;
import java.util.ArrayList;


/**
 * Service class for handling Keycloak token operations
 */
@Data
@Slf4j
@ApplicationScoped
public class KeycloakService {

    // Configuration parameters
    private final String HIGH_LEVEL_PERMISSION_USERNAME = ConfigProvider.getConfig().getValue("keycloak.high-level-permission-username", String.class);
    private final String HIGH_LEVEL_PERMISSION_PASSWORD = ConfigProvider.getConfig().getValue("keycloak.high-level-permission-password", String.class);
    private final int HIGH_LEVEL_PERMISSION_TOKEN_DURATION_IN_SECONDS = ConfigProvider.getConfig().getValue("keycloak.high-level-permission-token-validity-seconds", Integer.class);

    private final String AUTH_SERVER_URL = ConfigProvider.getConfig().getValue("keycloak.auth-server-url", String.class);
    private final String REALM = ConfigProvider.getConfig().getValue("keycloak.realm", String.class);

    private final String GENERIC_REST_CLIENT_ID = ConfigProvider.getConfig().getValue("keycloak.generic-rest-client.resource", String.class);
    private final String GENERIC_REST_CLIENT_SECRET = ConfigProvider.getConfig().getValue("keycloak.generic-rest-client.credentials.secret", String.class);

    private final String TOKEN_REST_CLIENT_ID = ConfigProvider.getConfig().getValue("keycloak.token-rest-client.resource", String.class);

    private final String TOKEN_REQUEST_ENDPOINT = ConfigProvider.getConfig().getValue("keycloak.token-request-endpoint", String.class);
    private final String TOKEN_REFRESH_ENDPOINT = ConfigProvider.getConfig().getValue("keycloak.token-refresh-endpoint", String.class);
    private final String TOKEN_VALIDATION_ENDPOINT = ConfigProvider.getConfig().getValue("keycloak.token-introspection-endpoint", String.class);
    private final String USER_CREATION_ENDPOINT = ConfigProvider.getConfig().getValue("keycloak.user-creation-endpoint", String.class);
    private final String USER_INFO_UPDATE_ENDPOINT = ConfigProvider.getConfig().getValue("keycloak.user-info-update-endpoint", String.class);
    private final String USER_PASSWORD_UPDATE_ENDPOINT_FIRST = ConfigProvider.getConfig().getValue("keycloak.user-password-update-endpoint-first", String.class);
    private final String USER_PASSWORD_UPDATE_ENDPOINT_SECOND = ConfigProvider.getConfig().getValue("keycloak.user-password-update-endpoint-second", String.class);
    private final String USER_ID_ENDPOINT = ConfigProvider.getConfig().getValue("keycloak.user-id-endpoint", String.class);

    private String cachedHighLevelPermissionToken;
    private Instant highLevelPermissionTokenExpiry;

    /**
     * Requests a token from Keycloak
     * @param authenticationRequestDTO the authentication request
     * @return a Response containing the token
     * @throws RuntimeException if an error occurs during the request
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

    /*
     * Refreshes a token with Keycloak (the client must possess refresh token).
     * @param refreshToken the refresh token
     * @return a Response containing the new token
     * @throws RuntimeException if an error occurs during the refresh
     */
    public Response refreshToken(String refreshToken) throws RuntimeException {

        Client client = ClientBuilder.newClient();

        Form requestBody = new Form()
                .param("grant_type", "refresh_token")
                .param("client_id", TOKEN_REST_CLIENT_ID)
                .param("refresh_token", refreshToken);

        try {
            return client.target(AUTH_SERVER_URL + REALM + TOKEN_REQUEST_ENDPOINT)
                    .request()
                    .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                    .accept("application/json")
                    .post(Entity.form(requestBody));
        } catch (Exception e) {
            throw new RuntimeException("Failed to refresh token: " + e.getMessage());
        }
    }

    /**
     * Validates a token with Keycloak
     * @param token the token to validate
     * @return a Response containing the result of the validation
     * @throws RuntimeException if an error occurs during the validation
     */
    public Response validateToken(String token) throws RuntimeException{

        Client client = ClientBuilder.newClient();

        Form requestBody = new Form()
                .param("token", token)
                .param("client_id", GENERIC_REST_CLIENT_ID)
                .param("client_secret", GENERIC_REST_CLIENT_SECRET);

        try {
            return client.target(AUTH_SERVER_URL + REALM + TOKEN_VALIDATION_ENDPOINT)
                    .request()
                    .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                    .accept("application/json")
                    .post(Entity.form(requestBody));
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to validate token: " + e.getMessage());
        }
    }

    /**
     * Creates a user in Keycloak
     * @param userCreationRequestDTO the user creation request
     * @return a Response containing the result of the user creation
     * @throws RuntimeException if an error occurs during the creation
     */
    public Response createUser(UserCreationRequestDTO userCreationRequestDTO) throws RuntimeException {

        JSONObject requestBody = new JSONObject()
                .put("username", userCreationRequestDTO.getUsername())
                .put("enabled", true)
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
            try (Response response = client.target(AUTH_SERVER_URL + USER_CREATION_ENDPOINT)
                    .request()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + getHighLevelPermissionToken())
                    .accept("application/json")
                    .post(Entity.json(requestBody))) {


                if (response.getStatus() == 201) {
                    return Response.status(response.getStatus()).entity(response.getEntity()).build();
                } else if (response.getStatus() == 409) {
                    log.warn("User {} already exists", userCreationRequestDTO.getUsername());
                    return Response.status(Response.Status.CONFLICT)
                            .entity("User with the given username or email already exists.")
                            .build();
                } else {
                    log.error("{} {}", response.getStatus(), response.readEntity(String.class));
                    throw new RuntimeException("Failed to create user. Status code: " + response.getStatus());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create user: " + e.getMessage());
        }
    }

    /**
     * Updates user information in Keycloak
     * @param userInfoUpdateRequestDTO the user information update request
     * @return a Response containing the result of the user information update
     * @throws RuntimeException if an error occurs during the update
     */
    public Response updateUserInfo(UserInfoUpdateRequestDTO userInfoUpdateRequestDTO) throws RuntimeException {

        JSONObject requestBody = new JSONObject();

        if (userInfoUpdateRequestDTO.getUserId() == null || userInfoUpdateRequestDTO.getToken() == null) {
            throw new IllegalArgumentException("User ID and Token must not be null");
        }

        if (userInfoUpdateRequestDTO.getEmail() != null)
            requestBody.put("email", userInfoUpdateRequestDTO.getEmail());

        if (userInfoUpdateRequestDTO.getFirstName() != null)
            requestBody.put("firstName", userInfoUpdateRequestDTO.getFirstName());

        if (userInfoUpdateRequestDTO.getLastName() != null)
            requestBody.put("lastName", userInfoUpdateRequestDTO.getLastName());

        Client client = ClientBuilder.newClient();

        try {
            try (Response response = client.target(AUTH_SERVER_URL + USER_INFO_UPDATE_ENDPOINT + "/" + userInfoUpdateRequestDTO.getUserId())
                    .request()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + userInfoUpdateRequestDTO.getToken())
                    .accept("application/json")
                    .put(Entity.json(requestBody))) {

                if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
                    return Response.status(response.getStatus()).entity(response.getEntity()).build();
                } else {
                    log.error("{} {}", response.getStatus(), response.readEntity(String.class));
                    throw new RuntimeException("Failed to update the user. Status code: " + response.getStatus());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to update the user: " + e.getMessage());
        }
    }

    /**
     * Update user password in Keycloak. Only an admin token owner may perform this operation
     *
     * @param userPasswordUpdateRequestDTO the user password update request
     * @return a Response containing the result of the user password update
     * @throws RuntimeException if an error occurs during the update
     */
    public Response updateUserPassword(UserPasswordUpdateRequestDTO userPasswordUpdateRequestDTO) throws RuntimeException {
        JSONObject requestBody = new JSONObject()
                .put("type", "password")
                .put("value", userPasswordUpdateRequestDTO.getNewPassword())
                .put("temporary", false);

        Client client = ClientBuilder.newClient();

        try {
            return client.target(AUTH_SERVER_URL + USER_PASSWORD_UPDATE_ENDPOINT_FIRST + "/"
                            + getUserId(userPasswordUpdateRequestDTO.getUsername()) + USER_PASSWORD_UPDATE_ENDPOINT_SECOND)
                    .request()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + getHighLevelPermissionToken())
                    .accept("application/json")
                    .put(Entity.json(requestBody));

        } catch (Exception e) {
            throw new RuntimeException("Failed to update the user password: " + e.getMessage());
        }
    }

    /**
     * This represents the actual request the user will send for a password update.
     * In order to update its password, the user must know the old password.
     * First a new token request will be made to validate the user. If the user is not valid,
     * the request will be rejected. If the user is valid, the password update request will be made.
     *
     * @param userPasswordUpdateRequestDTO the user password update request
     * @return a Response containing the result of the user password update
     * @throws RuntimeException if an error occurs during the update
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

    //TODO: Assign a user to a group

    //TODO: Unassign a user to a group

    /**
     * Returns a high level permission token for admin operations.
     * !!! Under no circumstances, this method must not be callable directly from rest endpoints !!!
     * @return a token with high level permissions
     * @throws RuntimeException if an error occurs during the token request
     */
    private String getHighLevelPermissionToken() {

        if (cachedHighLevelPermissionToken != null &&
                highLevelPermissionTokenExpiry == null &&
                Instant.now().isAfter(highLevelPermissionTokenExpiry.minusSeconds(HIGH_LEVEL_PERMISSION_TOKEN_DURATION_IN_SECONDS))) {
            return cachedHighLevelPermissionToken;
        }

        Form requestBody = new Form()
                .param("grant_type", "password")
                .param("client_id", TOKEN_REST_CLIENT_ID)
                .param("username", HIGH_LEVEL_PERMISSION_USERNAME)
                .param("password", HIGH_LEVEL_PERMISSION_PASSWORD);

        Client client = ClientBuilder.newClient();

        try {
            try (Response response = client.target(AUTH_SERVER_URL + REALM + TOKEN_REQUEST_ENDPOINT)
                    .request()
                    .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                    .accept("application/json")
                    .post(Entity.form(requestBody))) {

                if (response.getStatus() == 200) {
                    JSONObject jsonResponse = new JSONObject(response.readEntity(String.class));
                    return jsonResponse.get("access_token").toString();
                } else {
                    throw new RuntimeException("Failed to obtain token: Status code: " + response.getStatus());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to obtain token: " + e.getMessage());
        }
    }

    /**
     * Get user information from Keycloak. It might return more than one user since it checks if the parameter
     * passed as argument is contained in all the usernames.
     * Beware this method uses a high level permission token. It mustn't be called from rest endpoints.
     * @param username the username of the user
     * @return a Response containing the user information. The body of the response is a JSON array
     * @throws RuntimeException if an error occurs during the request
     */
    private Response getUserInfo(String username) throws RuntimeException {
        Client client = ClientBuilder.newClient();

        try {
            return client.target(AUTH_SERVER_URL + USER_ID_ENDPOINT + "?username=" + username)
                    .request()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + getHighLevelPermissionToken())
                    .accept("application/json")
                    .get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to obtain user id: " + e.getMessage());
        }
    }

    /**
     * Get the user id from Keycloak.
     * This method uses a high level permission token. It mustn't be called from rest endpoints.
     *
     * @param username the username of the user
     * @return the user id. If no user is found, it returns null
     * @throws RuntimeException if an error occurs during the request
     */
    private String getUserId(String username) throws RuntimeException {
        try {
            Response response = getUserInfo(username);
            JSONArray jsonArrayResponse = new JSONArray(response.readEntity(String.class));

            for (int i = 0; i < jsonArrayResponse.length(); ++i) {
                JSONObject jsonObject = jsonArrayResponse.getJSONObject(i);
                if (jsonObject.getString("username").equals(username)) {
                    return jsonObject.getString("id");
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to obtain user id: " + e.getMessage());
        }

        return null;
    }
}