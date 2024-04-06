package controller.dto;

import lombok.Data;

/*
 * Only updateable fields are included in this DTO
 */
@Data
public class UserInfoUpdateRequestDTO {
    private String userId = null;
    private String email = null;
    private String firstName = null;
    private String lastName = null;
    private String token = null;
}
