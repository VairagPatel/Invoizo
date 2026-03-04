package in.invoizo.invoicegeneratorapi.controller;

import in.invoizo.invoicegeneratorapi.entity.Invoice;
import in.invoizo.invoicegeneratorapi.service.PaymentService;
import in.invoizo.invoicegeneratorapi.service.RazorpayService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks/razorpay")
@Slf4j
@CrossOrigin(origins = "*")
public class RazorpayWebhookController {

    private final RazorpayService razorpayService;
    private final PaymentService paymentService;

    @Autowired
    public RazorpayWebhookController(@Autowired(required = false) RazorpayService razorpayService,
                                    PaymentService paymentService) {
        this.razorpayService = razorpayService;
        this.paymentService = paymentService;
    }

    /**
     * Handle Razorpay webhook events
     * Webhook URL: https://your-domain.com/api/webhooks/razorpay
     */
    @PostMapping
    public ResponseEntity<?> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {
        
        log.info("=== RAZORPAY WEBHOOK RECEIVED ===");
        log.info("Timestamp: {}", Instant.now());
        
        if (razorpayService == null) {
            log.warn("Razorpay not configured. Webhook ignored.");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Razorpay not configured"));
        }

        log.info("Payload received (length: {} bytes)", payload.length());
        log.info("Signature present: {}", signature != null && !signature.isEmpty());

        try {
            // Verify webhook signature for security
            if (signature == null || signature.isEmpty()) {
                log.error("Missing webhook signature");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Missing signature"));
            }

            boolean isValid = razorpayService.verifyWebhookSignature(payload, signature);
            if (!isValid) {
                log.error("Invalid webhook signature");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid signature"));
            }

            // Parse webhook payload
            JSONObject webhookData = new JSONObject(payload);
            String event = webhookData.getString("event");
            String webhookEventId = webhookData.optString("id", "");

            log.info("✅ Webhook signature verified successfully");
            log.info("Processing webhook event: {} (ID: {})", event, webhookEventId);

            // Handle different webhook events
            switch (event) {
                case "payment_link.paid":
                    handlePaymentLinkPaid(webhookData, webhookEventId);
                    break;
                    
                case "payment.captured":
                    handlePaymentCaptured(webhookData, webhookEventId);
                    break;
                    
                case "payment.failed":
                    handlePaymentFailed(webhookData);
                    break;
                    
                default:
                    log.info("Unhandled webhook event: {}", event);
            }

            return ResponseEntity.ok(Map.of("status", "success"));
            
        } catch (Exception e) {
            log.error("Error processing webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Webhook processing failed: " + e.getMessage()));
        }
    }

    /**
     * Handle payment_link.paid event
     */
    private void handlePaymentLinkPaid(JSONObject webhookData, String webhookEventId) {
        try {
            JSONObject payload = webhookData.getJSONObject("payload");
            JSONObject paymentLink = payload.getJSONObject("payment_link");
            JSONObject entity = paymentLink.getJSONObject("entity");
            
            // Extract invoice ID from reference_id
            String invoiceId = entity.optString("reference_id", "");
            if (invoiceId.isEmpty()) {
                log.error("Invoice ID not found in webhook payload");
                return;
            }

            // Extract payment details
            JSONObject payment = payload.optJSONObject("payment");
            if (payment == null) {
                payment = entity.optJSONObject("payments");
                if (payment != null && payment.length() > 0) {
                    payment = payment.getJSONObject(payment.keys().next());
                }
            }

            if (payment != null) {
                JSONObject paymentEntity = payment.optJSONObject("entity");
                if (paymentEntity == null) {
                    paymentEntity = payment;
                }

                String paymentId = paymentEntity.optString("id", "");
                String orderId = paymentEntity.optString("order_id", "");
                
                // Extract payment method
                Invoice.PaymentMethod paymentMethod = razorpayService.extractPaymentMethod(paymentEntity);

                log.info("💰 Processing payment for invoice: {}, paymentId: {}, method: {}", 
                    invoiceId, paymentId, paymentMethod);

                // Process payment success with idempotency
                paymentService.processPaymentSuccess(
                    invoiceId, 
                    paymentId, 
                    orderId, 
                    "", 
                    paymentMethod,
                    webhookEventId
                );
                
                log.info("✅ Payment processed successfully for invoice: {}", invoiceId);
            }
        } catch (Exception e) {
            log.error("Error handling payment_link.paid event", e);
        }
    }

    /**
     * Handle payment.captured event
     */
    private void handlePaymentCaptured(JSONObject webhookData, String webhookEventId) {
        try {
            JSONObject payload = webhookData.getJSONObject("payload");
            JSONObject payment = payload.getJSONObject("payment");
            JSONObject entity = payment.getJSONObject("entity");

            // Extract invoice ID from notes
            JSONObject notes = entity.optJSONObject("notes");
            if (notes == null) {
                log.warn("Notes not found in payment.captured webhook");
                return;
            }

            String invoiceId = notes.optString("invoice_id", "");
            if (invoiceId.isEmpty()) {
                log.error("Invoice ID not found in payment notes");
                return;
            }

            String paymentId = entity.getString("id");
            String orderId = entity.optString("order_id", "");
            
            // Extract payment method
            Invoice.PaymentMethod paymentMethod = razorpayService.extractPaymentMethod(entity);

            log.info("💰 Processing captured payment for invoice: {}, paymentId: {}, method: {}", 
                invoiceId, paymentId, paymentMethod);

            // Process payment success with idempotency
            paymentService.processPaymentSuccess(
                invoiceId, 
                paymentId, 
                orderId, 
                "", 
                paymentMethod,
                webhookEventId
            );
            
            log.info("✅ Payment captured and processed successfully for invoice: {}", invoiceId);
        } catch (Exception e) {
            log.error("Error handling payment.captured event", e);
        }
    }

    /**
     * Handle payment.failed event
     */
    private void handlePaymentFailed(JSONObject webhookData) {
        try {
            JSONObject payload = webhookData.getJSONObject("payload");
            JSONObject payment = payload.getJSONObject("payment");
            JSONObject entity = payment.getJSONObject("entity");

            String paymentId = entity.getString("id");
            String errorDescription = entity.optString("error_description", "Payment failed");

            log.warn("Payment failed: paymentId={}, error={}", paymentId, errorDescription);
            
            // You can add logic here to update invoice status or send notification
            
        } catch (Exception e) {
            log.error("Error handling payment.failed event", e);
        }
    }

    /**
     * Health check endpoint for webhook
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "service", "razorpay-webhook",
            "timestamp", System.currentTimeMillis()
        ));
    }
}
