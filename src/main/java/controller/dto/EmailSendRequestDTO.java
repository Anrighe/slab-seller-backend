package controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import service.EmailService;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailSendRequestDTO {

    private EmailAddressDTO from;
    private List<EmailAddressDTO> to;
    private String subject;
    private String text;
    private String html;
    private List<Map<String, Object>> personalization;
}