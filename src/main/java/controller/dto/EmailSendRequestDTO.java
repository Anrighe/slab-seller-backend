package controller.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class EmailSendRequestDTO {

    private EmailAddressDTO from;
    private List<EmailAddressDTO> to;
    private String subject;
    private String text;
    private String html;
    private List<Map<String, Object>> personalization;
}