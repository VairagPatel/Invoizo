package in.invoizo.invoicegeneratorapi.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RazorpayConfig {

    @Value("${razorpay.key.id:}")
    private String keyId;

    @Value("${razorpay.key.secret:}")
    private String keySecret;

    @Value("${razorpay.webhook.secret:}")
    private String webhookSecret;

    @Bean
    public RazorpayClient razorpayClient() {
        try {
            if (keyId == null || keyId.isEmpty() || keySecret == null || keySecret.isEmpty()) {
                log.warn("Razorpay credentials not configured. Payment features will be disabled.");
                return null;
            }
            log.info("Initializing Razorpay client with key: {}", keyId.substring(0, Math.min(8, keyId.length())) + "...");
            return new RazorpayClient(keyId, keySecret);
        } catch (RazorpayException e) {
            log.error("Failed to initialize Razorpay client", e);
            return null;
        }
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }

    public boolean isRazorpayEnabled() {
        return keyId != null && !keyId.isEmpty() && keySecret != null && !keySecret.isEmpty();
    }
}
