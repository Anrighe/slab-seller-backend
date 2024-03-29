package service;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.HashMap;

@Data
@Slf4j
@ApplicationScoped
public class KeycloakTokenService {

    //@ConfigProperty(name = "keycloak.auth-server-url")
    String authServerUrl = "http://localhost:8443/realms/";

    //@ConfigProperty(name = "keycloak.realm")
    String realm = "slab-seller";

    //@ConfigProperty(name = "keycloak.resource")
    String clientId = "admin-rest-client";

    //@ConfigProperty(name = "keycloak.credentials.secret")
    String clientSecret = "NhsqkAGlZnZnUoB2QVxC6VTWCEPGqp9A";

    //@ConfigProperty(name = "keycloak.api.token-request-endpoint")
    String tokenRequestEndpoint = "/protocol/openid-connect/token";

    public String requestToken(String username, String password) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        Map<Object, Object> data = new HashMap<>();
        data.put("grant_type", "password");
        data.put("client_id", "admin-rest-client");
        data.put("client_secret", "NhsqkAGlZnZnUoB2QVxC6VTWCEPGqp9A");
        data.put("username", "developer");
        data.put("password", "123");

        log.debug("URL: " + authServerUrl + realm + tokenRequestEndpoint);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8443/realms/slab-seller/protocol/openid-connect/token"))
                .POST(buildFormDataFromMap(data))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body();
        } else {
            throw new RuntimeException("Failed to obtain token. Status code: " + response.statusCode());
        }
    }

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
