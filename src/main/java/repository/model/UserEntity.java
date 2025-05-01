package repository.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserEntity {
    public String id;
    public String username;
    public String email;
    public String firstName;
    public String lastName;
    public boolean enabled;
    public boolean emailVerified;
    public Map<String, Boolean> access;
    public boolean totp;
    public Instant createdTimestamp;
    public int notBefore;
    public ArrayList<Object> disableableCredentialTypes;
    public ArrayList<Object> requiredActions;


    public UserEntity(JSONObject json) {
        this.id = json.optString("id", null);
        this.username = json.optString("username", null);
        this.email = json.optString("email", null);
        this.firstName = json.optString("firstName", null);
        this.lastName = json.optString("lastName", null);
        this.enabled = json.optBoolean("enabled", false);
        this.emailVerified = json.optBoolean("emailVerified", false);
        this.totp = json.optBoolean("totp", false);
        this.notBefore = json.optInt("notBefore", 0);

        if (json.has("createdTimestamp")) {
            this.createdTimestamp = Instant.ofEpochMilli(json.getLong("createdTimestamp"));
        }

        if (json.has("access")) {
            this.access = new HashMap<>();
            JSONObject accessJson = json.getJSONObject("access");
            for (String key : accessJson.keySet()) {
                this.access.put(key, accessJson.getBoolean(key));
            }
        }

        this.disableableCredentialTypes = new ArrayList<>();
        if (json.has("disableableCredentialTypes")) {
            for (Object object : json.getJSONArray("disableableCredentialTypes")) {
                this.disableableCredentialTypes.add(object);
            }
        }

        this.requiredActions = new ArrayList<>();
        if (json.has("requiredActions")) {
            for (Object object : json.getJSONArray("requiredActions")) {
                this.requiredActions.add(object);
            }
        }
    }
}
