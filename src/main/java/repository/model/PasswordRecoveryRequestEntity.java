package repository.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordRecoveryRequestEntity {
    private int id;
    private String email;
    private LocalDateTime sendTime;
    private LocalDateTime expiryTime;
    private boolean used = false;
    private LocalDateTime usedTime;
    private String hashedId;
    private boolean disabled;


    public PasswordRecoveryRequestEntity(String email, String hashedId) {
        this.email = email;
        this.hashedId = hashedId;
    }

    /**
     * Checks if the password recovery request is valid
     * @return true if the password recovery request has not been used, disabled or expired; false otherwise
     */
    public boolean isValid() {
        return !used && !disabled && expiryTime != null && !expiryTime.isBefore(LocalDateTime.now());
    }
}
