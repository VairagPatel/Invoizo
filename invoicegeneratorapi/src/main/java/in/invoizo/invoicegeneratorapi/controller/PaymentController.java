package in.invoizo.invoicegeneratorapi.controller;

import in.invoizo.invoicegeneratorapi.entity.Invoice;
import in.invoizo.invoicegeneratorapi.service.InvoiceService;
import in.invoizo.invoicegeneratorapi.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;
    private final InvoiceService invoiceService;

    /**
     * Generate Razorpay payment link for an invoice
     */
    @PostMapping("/generate-link/{invoiceId}")
    public ResponseEntity<?> generatePaymentLink(@PathVariable String invoiceId, Authentication authentication) {
        log.info("=== GENERATE PAYMENT LINK REQUEST ===");
        log.info("Invoice ID: {}", invoiceId);
        
        try {
            String clerkId = authentication.getName();
            if (clerkId == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Authentication required"
                ));
            }
            
            // Validate invoice ownership
            Invoice invoice = invoiceService.getInvoiceById(invoiceId);
            if (invoice == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invoice not found"
                ));
            }
            
            if (!clerkId.equals(invoice.getClerkId())) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Access denied"
                ));
            }
            
            String paymentLink = paymentService.generatePaymentLink(invoiceId);
            
            if (paymentLink == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Razorpay not configured. Please configure Razorpay credentials."
                ));
            }
            
            log.info("Payment link generated successfully: {}", paymentLink);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "paymentLink", paymentLink,
                "message", "Payment link generated successfully"
            ));
        } catch (Exception e) {
            log.error("Error generating payment link for invoice {}", invoiceId, e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to generate payment link: " + e.getMessage()
            ));
        }
    }

    /**
     * Send invoice with payment link via email
     */
    @PostMapping("/send-invoice/{invoiceId}")
    public ResponseEntity<?> sendInvoiceWithPaymentLink(@PathVariable String invoiceId, Authentication authentication) {
        log.info("=== SEND INVOICE WITH PAYMENT LINK ===");
        log.info("Invoice ID: {}", invoiceId);
        
        try {
            String clerkId = authentication.getName();
            if (clerkId == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Authentication required"
                ));
            }
            
            // Validate invoice ownership
            Invoice invoice = invoiceService.getInvoiceById(invoiceId);
            if (invoice == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invoice not found"
                ));
            }
            
            if (!clerkId.equals(invoice.getClerkId())) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Access denied"
                ));
            }
            
            paymentService.sendInvoiceWithPaymentLink(invoiceId);
            
            log.info("Invoice sent with payment link successfully");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Invoice sent with payment link successfully"
            ));
        } catch (Exception e) {
            log.error("Error sending invoice with payment link {}", invoiceId, e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to send invoice: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/mark-cash-payment/{invoiceId}")
    public ResponseEntity<?> markCashPayment(@PathVariable String invoiceId, Authentication authentication) {
        log.info("=== CASH PAYMENT REQUEST ===");
        log.info("Invoice ID: {}", invoiceId);
        log.info("User: {}", authentication != null ? authentication.getName() : "null");
        
        try {
            String clerkId = authentication.getName();
            if (clerkId == null) {
                log.error("Authentication failed - no clerk ID");
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Authentication required"
                ));
            }
            
            // Validate that the invoice belongs to the authenticated user
            Invoice invoice = invoiceService.getInvoiceById(invoiceId);
            if (invoice == null) {
                log.error("Invoice {} not found", invoiceId);
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invoice not found"
                ));
            }
            
            if (!clerkId.equals(invoice.getClerkId())) {
                log.error("Access denied - invoice belongs to different user");
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Access denied: Invoice does not belong to the authenticated user"
                ));
            }
            
            paymentService.markCashPayment(invoiceId);
            log.info("Cash payment marked successfully for invoice: {}", invoiceId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cash payment marked successfully"
            ));
        } catch (Exception e) {
            log.error("Error marking cash payment for invoice {}", invoiceId, e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to mark cash payment: " + e.getMessage()
            ));
        }
    }
}