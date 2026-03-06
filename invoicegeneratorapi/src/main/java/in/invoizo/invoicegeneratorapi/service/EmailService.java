package in.invoizo.invoicegeneratorapi.service;

import in.invoizo.invoicegeneratorapi.entity.Invoice;
import in.invoizo.invoicegeneratorapi.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    
    private final ValidationUtil validationUtil;
    private final SendGridEmailService sendGridEmailService;

    public void sendInvoiceEmail(String toEmail, MultipartFile file) throws IOException {
        validationUtil.validateEmail(toEmail);
        
        String subject = "Your Invoice";
        String htmlContent = "<p>Dear Customer,</p><p>Please find attached your invoice.</p><p>Thank you!</p>";
        
        sendGridEmailService.sendEmail(toEmail, subject, htmlContent);
    }

    public void sendInvoiceWithPaymentLink(Invoice invoice, String paymentLink) {
        try {
            String toEmail = invoice.getBilling().getEmail();
            if (toEmail == null || toEmail.isEmpty()) {
                toEmail = invoice.getBilling().getPhone();
            }
            
            if (toEmail == null || toEmail.isEmpty() || !toEmail.contains("@")) {
                throw new IllegalArgumentException("Valid email address not found in invoice billing details");
            }
            
            String subject = "Invoice #" + invoice.getInvoice().getNumber() + " - Payment Link";
            String htmlContent = buildInvoiceEmailWithPaymentLink(invoice, paymentLink);
            
            sendGridEmailService.sendEmail(toEmail, subject, htmlContent);
            log.info("Invoice email sent successfully to: {}", toEmail);
        } catch (IOException e) {
            log.error("Failed to send invoice email: {}", e.getMessage());
            throw new RuntimeException("Failed to send invoice email: " + e.getMessage(), e);
        }
    }

    public void sendPaymentConfirmation(Invoice invoice) {
        try {
            String toEmail = invoice.getBilling().getEmail();
            if (toEmail == null || toEmail.isEmpty()) {
                toEmail = invoice.getBilling().getPhone();
            }
            
            if (toEmail == null || toEmail.isEmpty() || !toEmail.contains("@")) {
                log.warn("Valid email address not found for payment confirmation. Skipping email.");
                return;
            }
            
            String subject = "Payment Received - Invoice #" + invoice.getInvoice().getNumber();
            String htmlContent = buildPaymentConfirmationEmail(invoice);
            
            sendGridEmailService.sendEmail(toEmail, subject, htmlContent);
            log.info("Payment confirmation email sent successfully to: {}", toEmail);
        } catch (IOException e) {
            log.error("Failed to send payment confirmation: {}", e.getMessage());
        }
    }

    private String buildInvoiceEmailWithPaymentLink(Invoice invoice, String paymentLink) {
        double totalAmount = calculateTotalAmount(invoice);
        
        StringBuilder html = new StringBuilder();
        html.append("<html><body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">");
        html.append("<div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">");
        html.append("<h2 style=\"color: #2c3e50;\">Invoice from ").append(invoice.getCompany().getName()).append("</h2>");
        html.append("<p>Dear ").append(invoice.getBilling().getName()).append(",</p>");
        html.append("<p>Thank you for your business! Please find your invoice details below:</p>");
        html.append("<div style=\"background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;\">");
        html.append("<p><strong>Invoice Number:</strong> ").append(invoice.getInvoice().getNumber()).append("</p>");
        html.append("<p><strong>Invoice Date:</strong> ").append(invoice.getInvoice().getDate()).append("</p>");
        html.append("<p><strong>Due Date:</strong> ").append(invoice.getInvoice().getDueDate()).append("</p>");
        html.append("<p><strong>Total Amount:</strong> ₹").append(String.format("%.2f", totalAmount)).append("</p>");
        html.append("</div>");
        html.append("<div style=\"background-color: #e8f5e9; padding: 15px; border-radius: 5px; margin: 20px 0;\">");
        html.append("<h3 style=\"color: #2e7d32; margin-top: 0;\">Pay Online</h3>");
        html.append("<p>Click the button below to pay securely online:</p>");
        html.append("<a href=\"").append(paymentLink).append("\" style=\"display: inline-block; background-color: #4CAF50; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; margin: 10px 0;\">Pay Now</a>");
        html.append("<p style=\"font-size: 12px; color: #666;\">Or copy this link: ").append(paymentLink).append("</p>");
        html.append("</div>");
        html.append("<p>If you have any questions, please don't hesitate to contact us.</p>");
        html.append("<p>Best regards,<br>").append(invoice.getCompany().getName()).append("</p>");
        html.append("<hr style=\"border: none; border-top: 1px solid #ddd; margin: 30px 0;\">");
        html.append("<p style=\"font-size: 12px; color: #666;\">This is an automated email.</p>");
        html.append("</div></body></html>");
        
        return html.toString();
    }

    private String buildPaymentConfirmationEmail(Invoice invoice) {
        double totalAmount = calculateTotalAmount(invoice);
        String paymentMethod = invoice.getPaymentDetails() != null ? 
            invoice.getPaymentDetails().getPaymentMethod().toString() : "Unknown";
        
        StringBuilder html = new StringBuilder();
        html.append("<html><body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">");
        html.append("<div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">");
        html.append("<div style=\"background-color: #d4edda; padding: 20px; border-radius: 5px; text-align: center;\">");
        html.append("<h2 style=\"color: #155724; margin: 0;\">✓ Payment Received</h2>");
        html.append("</div>");
        html.append("<p>Dear ").append(invoice.getBilling().getName()).append(",</p>");
        html.append("<p>We have successfully received your payment. Thank you!</p>");
        html.append("<div style=\"background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;\">");
        html.append("<p><strong>Invoice Number:</strong> ").append(invoice.getInvoice().getNumber()).append("</p>");
        html.append("<p><strong>Amount Paid:</strong> ₹").append(String.format("%.2f", totalAmount)).append("</p>");
        html.append("<p><strong>Payment Method:</strong> ").append(paymentMethod).append("</p>");
        html.append("</div>");
        html.append("<p>Thank you for your business!</p>");
        html.append("<p>Best regards,<br>").append(invoice.getCompany().getName()).append("</p>");
        html.append("</div></body></html>");
        
        return html.toString();
    }

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
