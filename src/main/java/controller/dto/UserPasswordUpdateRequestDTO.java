package controller.dto;

import lombok.Data;

@Data
public class UserPasswordUpdateRequestDTO {
    private String userId;
    private String username;
    private String newPassword;
    private String oldPassword;
}
