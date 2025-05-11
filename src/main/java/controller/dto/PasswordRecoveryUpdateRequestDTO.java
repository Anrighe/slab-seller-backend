package controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PasswordRecoveryUpdateRequestDTO {
    public String passwordRecoveryRequestHashedId;
    public String passwordRecoveryEmail;
    public String newPassword;
    public String confirmPassword;
}

