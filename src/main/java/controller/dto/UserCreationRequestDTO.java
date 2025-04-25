package controller.dto;

import lombok.Data;

@Data
public class UserCreationRequestDTO {
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
}
