package in.invoizo.invoicegeneratorapi.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class SendGridEmailService {

    @Value("${sendgrid.api.key:}")
    private String sendGridApiKey;

    @Value("${spring.mail.username:noreply@invoizo.com}")
    private String fromEmail;

    public void sendEmail(String toEmail, String subject, String htmlContent) throws IOException {
        if (sendGridApiKey == null || sendGridApiKey.isEmpty()) {
            log.warn("SendGrid API key not configured. Email not sent.");
            return;
        }

        Email from = new Email(fromEmail);
        Email to = new Email(toEmail);
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sg.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("Email sent successfully to: {}", toEmail);
            } else {
                log.error("Failed to send email. Status: {}, Body: {}", 
                    response.getStatusCode(), response.getBody());
            }
        } catch (IOException ex) {
            log.error("Error sending email via SendGrid", ex);
            throw ex;
        }
    }
}
