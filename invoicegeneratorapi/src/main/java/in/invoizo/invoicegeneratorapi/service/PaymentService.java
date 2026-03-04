package in.invoizo.invoicegeneratorapi.service;

import com.razorpay.RazorpayException;
import in.invoizo.invoicegeneratorapi.entity.Invoice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
public class PaymentService {

    private final InvoiceService invoiceService;
    private final RazorpayService razorpayService;
    private final EmailService emailService;

    @Autowired
    public PaymentService(InvoiceService invoiceService,
                         @Autowired(required = false) RazorpayService razorpayService,
                         EmailService emailService) {
        this.invoiceService = invoiceService;
        this.razorpayService = razorpayService;
        this.emailService = emailService;
    }

    /**
     * Generate Razorpay payment link and attach to invoice
     */
    public String generatePaymentLink(String invoiceId) {
        if (razorpayService == null) {
            log.warn("RazorpayService not available. Cannot generate payment link.");
            return null;
        }
        
        try {
            Invoice invoice = invoiceService.getInvoiceById(invoiceId);
            
            // Generate Razorpay payment link
            String paymentLink = razorpayService.generatePaymentLink(invoice);
            
            if (paymentLink == null) {
                log.warn("Razorpay not configured. Skipping payment link generation.");
                return null;
            }

            // Update invoice with payment link
            Invoice.PaymentDetails paymentDetails = invoice.getPaymentDetails();
            if (paymentDetails == null) {
                paymentDetails = new Invoice.PaymentDetails();
            }

            paymentDetails.setPaymentLink(paymentLink);
            paymentDetails.setPaymentLinkCreatedAt(Instant.now());
            paymentDetails.setPaymentStatus(Invoice.PaymentStatus.PENDING);
            paymentDetails.setCurrency("INR");
            paymentDetails.setCashPaymentAllowed(true); // Allow both online and cash

            invoice.setPaymentDetails(paymentDetails);
            invoiceService.updateInvoice(invoice);

            log.info("Payment link generated and attached to invoice: {}", invoiceId);
            return paymentLink;
        } catch (RazorpayException e) {
            log.error("Failed to generate payment link for invoice: {}", invoiceId, e);
            throw new RuntimeException("Failed to generate payment link: " + e.getMessage(), e);
        }
    }

    /**
     * Send invoice with payment link via email
     */
    public void sendInvoiceWithPaymentLink(String invoiceId) {
        Invoice invoice = invoiceService.getInvoiceById(invoiceId);
        
        // Generate payment link if not already generated and razorpay is available
        if (razorpayService != null && 
            (invoice.getPaymentDetails() == null || invoice.getPaymentDetails().getPaymentLink() == null)) {
            generatePaymentLink(invoiceId);
            invoice = invoiceService.getInvoiceById(invoiceId); // Refresh
        }

        // Send email with payment link (or without if razorpay not configured)
        String paymentLink = invoice.getPaymentDetails() != null ? 
            invoice.getPaymentDetails().getPaymentLink() : null;
        emailService.sendInvoiceWithPaymentLink(invoice, paymentLink);

        // Update invoice status to SENT
        invoice.setStatus(Invoice.InvoiceStatus.SENT);
        invoice.setEmailSentAt(Instant.now());
        invoiceService.updateInvoice(invoice);

        log.info("Invoice sent with payment link: {}", invoiceId);
    }

    /**
     * Process Razorpay webhook payment success
     */
    public void processPaymentSuccess(String invoiceId, String paymentId, String orderId, 
                                     String signature, Invoice.PaymentMethod paymentMethod,
                                     String webhookEventId) {
        Invoice invoice = invoiceService.getInvoiceById(invoiceId);

        // Check idempotency - prevent duplicate processing
        if (invoice.getPaymentDetails() != null && 
            webhookEventId.equals(invoice.getPaymentDetails().getWebhookEventId())) {
            log.info("Webhook event already processed: {}", webhookEventId);
            return;
        }

        Invoice.PaymentDetails paymentDetails = invoice.getPaymentDetails();
        if (paymentDetails == null) {
            paymentDetails = new Invoice.PaymentDetails();
        }

        paymentDetails.setPaymentStatus(Invoice.PaymentStatus.SUCCESS);
        paymentDetails.setPaymentMethod(paymentMethod);
        paymentDetails.setPaymentDate(Instant.now());
        paymentDetails.setRazorpayPaymentId(paymentId);
        paymentDetails.setRazorpayOrderId(orderId);
        paymentDetails.setRazorpaySignature(signature);
        paymentDetails.setPaymentReferenceId(paymentId);
        paymentDetails.setWebhookEventId(webhookEventId); // For idempotency

        invoice.setPaymentDetails(paymentDetails);
        invoice.setStatus(Invoice.InvoiceStatus.PAID);
        invoice.setPaidAt(Instant.now());

        invoiceService.updateInvoice(invoice);
        log.info("✅ Invoice {} automatically marked as PAID via Razorpay {}", 
            invoiceId, paymentMethod);
        log.info("Payment details - ID: {}, Order: {}, Amount: {}", 
            paymentId, orderId, invoice.getPaymentDetails().getTotalAmount());

        // Send payment confirmation email
        emailService.sendPaymentConfirmation(invoice);
        log.info("📧 Payment confirmation email sent for invoice: {}", invoiceId);
    }

    /**
     * Mark cash payment (existing functionality)
     */
    public void markCashPayment(String invoiceId) {
        Invoice invoice = invoiceService.getInvoiceById(invoiceId);
        
        Invoice.PaymentDetails paymentDetails = invoice.getPaymentDetails();
        if (paymentDetails == null) {
            paymentDetails = new Invoice.PaymentDetails();
        }

        paymentDetails.setPaymentStatus(Invoice.PaymentStatus.SUCCESS);
        paymentDetails.setPaymentMethod(Invoice.PaymentMethod.CASH);
        paymentDetails.setPaymentDate(Instant.now());

        invoice.setPaymentDetails(paymentDetails);
        invoice.setStatus(Invoice.InvoiceStatus.PAID);
        invoice.setPaidAt(Instant.now());

        invoiceService.updateInvoice(invoice);
        log.info("Cash payment marked for invoice {}", invoiceId);
    }
}
