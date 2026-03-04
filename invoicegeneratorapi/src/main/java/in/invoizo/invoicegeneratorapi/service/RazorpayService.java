package in.invoizo.invoicegeneratorapi.service;

import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import in.invoizo.invoicegeneratorapi.config.RazorpayConfig;
import in.invoizo.invoicegeneratorapi.entity.Invoice;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
public class RazorpayService {

    private final RazorpayClient razorpayClient;
    private final RazorpayConfig razorpayConfig;

    @Autowired
    public RazorpayService(@Autowired(required = false) RazorpayClient razorpayClient, 
                          RazorpayConfig razorpayConfig) {
        this.razorpayClient = razorpayClient;
        this.razorpayConfig = razorpayConfig;
    }

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Generate Razorpay payment link for an invoice
     */
    public String generatePaymentLink(Invoice invoice) throws RazorpayException {
        if (razorpayClient == null || !razorpayConfig.isRazorpayEnabled()) {
            log.warn("Razorpay is not configured. Cannot generate payment link.");
            return null;
        }

        try {
            double totalAmount = calculateTotalAmount(invoice);
            
            // Convert to paise (Razorpay uses smallest currency unit)
            int amountInPaise = (int) Math.round(totalAmount * 100);

            JSONObject paymentLinkRequest = new JSONObject();
            paymentLinkRequest.put("amount", amountInPaise);
            paymentLinkRequest.put("currency", "INR");
            paymentLinkRequest.put("accept_partial", false);
            paymentLinkRequest.put("description", "Payment for Invoice #" + invoice.getInvoice().getNumber());
            paymentLinkRequest.put("reference_id", invoice.getId());
            
            // Customer details
            JSONObject customer = new JSONObject();
            customer.put("name", invoice.getBilling().getName());
            customer.put("contact", invoice.getBilling().getPhone());
            paymentLinkRequest.put("customer", customer);

            // Notification settings
            JSONObject notify = new JSONObject();
            notify.put("sms", true);
            notify.put("email", false); // We'll send our own email
            paymentLinkRequest.put("notify", notify);

            // Callback URL
            paymentLinkRequest.put("callback_url", frontendUrl + "/payment-success");
            paymentLinkRequest.put("callback_method", "get");

            // Expiry - 30 days from now
            long expiryTimestamp = Instant.now().plus(30, ChronoUnit.DAYS).getEpochSecond();
            paymentLinkRequest.put("expire_by", expiryTimestamp);

            // Notes for reference
            JSONObject notes = new JSONObject();
            notes.put("invoice_id", invoice.getId());
            notes.put("invoice_number", invoice.getInvoice().getNumber());
            notes.put("customer_name", invoice.getBilling().getName());
            paymentLinkRequest.put("notes", notes);

            log.info("Creating Razorpay payment link for invoice: {}", invoice.getId());
            PaymentLink paymentLink = razorpayClient.paymentLink.create(paymentLinkRequest);

            String shortUrl = paymentLink.get("short_url");
            log.info("Payment link created successfully: {}", shortUrl);

            return shortUrl;
        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay payment link for invoice: {}", invoice.getId(), e);
            throw e;
        }
    }

    /**
     * Generate payment data for QR code
     * Returns the payment link URL that can be encoded in QR
     */
    public String generateQRData(String paymentLink) {
        if (paymentLink == null || paymentLink.isEmpty()) {
            log.warn("No payment link provided for QR generation");
            return null;
        }
        
        // The payment link itself can be used as QR data
        // When scanned, it will open the Razorpay payment page
        return paymentLink;
    }

    /**
     * Get payment link details including QR data
     */
    public JSONObject getPaymentLinkDetails(Invoice invoice, String paymentLink) {
        JSONObject details = new JSONObject();
        
        if (paymentLink != null) {
            details.put("paymentLink", paymentLink);
            details.put("qrData", generateQRData(paymentLink));
            details.put("paymentStatus", invoice.getStatus().toString());
            details.put("amount", calculateTotalAmount(invoice));
            details.put("currency", "INR");
            details.put("invoiceNumber", invoice.getInvoice().getNumber());
        }
        
        return details;
    }

    /**
     * Verify webhook signature for security
     */
    public boolean verifyWebhookSignature(String payload, String signature) {
        try {
            String webhookSecret = razorpayConfig.getWebhookSecret();
            if (webhookSecret == null || webhookSecret.isEmpty()) {
                log.warn("Webhook secret not configured. Skipping signature verification.");
                return false;
            }

            // Razorpay signature verification
            String expectedSignature = com.razorpay.Utils.getHash(payload, webhookSecret);
            boolean isValid = expectedSignature.equals(signature);
            
            if (!isValid) {
                log.error("Webhook signature verification failed");
            }
            
            return isValid;
        } catch (Exception e) {
            log.error("Error verifying webhook signature", e);
            return false;
        }
    }

    /**
     * Extract payment method from Razorpay payment data
     */
    public Invoice.PaymentMethod extractPaymentMethod(JSONObject payment) {
        try {
            String method = payment.optString("method", "").toUpperCase();
            return switch (method) {
                case "UPI" -> Invoice.PaymentMethod.RAZORPAY_UPI;
                case "CARD" -> Invoice.PaymentMethod.RAZORPAY_CARD;
                case "NETBANKING" -> Invoice.PaymentMethod.RAZORPAY_NETBANKING;
                case "WALLET" -> Invoice.PaymentMethod.RAZORPAY_WALLET;
                default -> Invoice.PaymentMethod.RAZORPAY_OTHER;
            };
        } catch (Exception e) {
            log.error("Error extracting payment method", e);
            return Invoice.PaymentMethod.RAZORPAY_OTHER;
        }
    }

    /**
     * Calculate total amount including GST
     */
    private double calculateTotalAmount(Invoice invoice) {
        double subtotal = invoice.getItems().stream()
                .mapToDouble(item -> item.getQty() * item.getAmount())
                .sum();

        double gstTotal = 0;
        if (invoice.getGstDetails() != null) {
            gstTotal = invoice.getGstDetails().getGstTotal();
        }

        double tax = invoice.getTax();

        return subtotal + gstTotal + tax;
    }
}
