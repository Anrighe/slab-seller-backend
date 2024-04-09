package controller.dto;

import lombok.Data;

@Data
public class UserPasswordUpdateRequestDTO {
    String userId;
    String newPassword;
    String token;
}
