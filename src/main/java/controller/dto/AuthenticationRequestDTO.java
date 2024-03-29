package controller.dto;

import lombok.Data;

@Data
public class AuthenticationRequestDTO {
    //private String grantType;
    //private String clientId;
    //private String clientSecret;
    private String username;
    private String password;
}
