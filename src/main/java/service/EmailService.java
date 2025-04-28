package service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import controller.dto.EmailAddressDTO;
import controller.dto.EmailSendRequestDTO;
import controller.dto.PasswordRecoveryRequestDTO;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.List;
import java.util.Map;

@Data
@Slf4j
@ApplicationScoped
public class EmailService {

    // Configuration parameters
    private final String EMAIL_API_TOKEN = ConfigProvider.getConfig().getValue("mailsender.api.token", String.class);
    private final String EMAIL_API_ENDPOINT = ConfigProvider.getConfig().getValue("mailsender.api.endpoint", String.class);
    private final String EMAIL_API_DOMAIN = ConfigProvider.getConfig().getValue("mailsender.api.domain", String.class);
    private final String EMAIL_API_SENDER_USER = ConfigProvider.getConfig().getValue("mailsender.api.sender.user", String.class);
    private final String EMAIL_API_SENDER_NAME = ConfigProvider.getConfig().getValue("mailsender.api.sender.name", String.class);
    private final String EMAIL_API_TEMPLATE_PASSWORD_RECOVERY_ID = ConfigProvider.getConfig().getValue("mailsender.template.password-recovery-id", String.class);

    private final String EMAIL_SENDER = EMAIL_API_SENDER_USER + "@" + EMAIL_API_DOMAIN;


    public Response sendEmail(PasswordRecoveryRequestDTO passwordRecoveryRequestDTO, Map<String, String> personalizationData) throws RuntimeException {

        List<Map<String, Object>> personalizationList = List.of(
                Map.of(
                        "email", passwordRecoveryRequestDTO.getEmail(),
                        "data", personalizationData
                )
        );

        EmailSendRequestDTO emailSendRequestDTO = new EmailSendRequestDTO(
                new EmailAddressDTO(EMAIL_SENDER, EMAIL_API_SENDER_NAME),
                List.of(new EmailAddressDTO(passwordRecoveryRequestDTO.getEmail(), "TEST")),
                "Slab seller - Password recovery",
                "Slab seller - Password recovery",
                "",
                personalizationList,
                EMAIL_API_TEMPLATE_PASSWORD_RECOVERY_ID
        );

        Client client = ClientBuilder.newClient();
        log.info("Sending {} email to {}" , emailSendRequestDTO.getTo(), emailSendRequestDTO.getSubject());

        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(emailSendRequestDTO);

            return client.target(EMAIL_API_ENDPOINT)
                    .request()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + EMAIL_API_TOKEN)
                    .accept("application/json")
                    .post(Entity.json(json));

        } catch (JsonProcessingException e) {
            String errorMessage = String.format("Json processing error during email sending: %s", e.getMessage());
            log.error(errorMessage);
            throw new RuntimeException(e);
        } catch (Exception e) {
            String errorMessage = String.format("Failed to send email to %s: %s", emailSendRequestDTO.getTo(), e.getMessage());
            log.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }
}
