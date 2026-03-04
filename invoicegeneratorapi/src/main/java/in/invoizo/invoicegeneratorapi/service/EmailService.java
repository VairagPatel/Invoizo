package in.invoizo.invoicegeneratorapi.service;

import in.invoizo.invoicegeneratorapi.entity.Invoice;
import in.invoizo.invoicegeneratorapi.util.ValidationUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final ValidationUtil validationUtil;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

    /**
     * Sends an invoice email
     * 
     * @param toEmail Recipient email address
     * @param file Invoice PDF file
     * @throws MessagingException if email sending fails
     * @throws IOException if file reading fails
     */
    public void sendInvoiceEmail(String toEmail, MultipartFile file) throws MessagingException, IOException {
        // Validate email address
        validationUtil.validateEmail(toEmail);
        
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Your Invoice");
        
        String textBody = "Dear Customer,\n\n" +
                        "Please find attached your invoice.\n\n" +
                        "Thank you!";
        helper.setText(textBody);
        
        helper.addAttachment(file.getOriginalFilename(), new ByteArrayResource(file.getBytes()));

        mailSender.send(message);
    }

    /**
     * Sends a payment reminder email
     * 
     * @param toEmail Recipient email address
     * @param subject Email subject
     * @param body Email body
     * @param invoice Invoice details for attachment
     * @throws MessagingException if email sending fails
     */
    public void sendPaymentReminderEmail(String toEmail, String subject, String body, Invoice invoice) throws MessagingException {
        // Validate email address
        validationUtil.validateEmail(toEmail);
        
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(body);

        mailSender.send(message);
    }

    /**
     * Sends an invoice email with payment link
     * 
     * @param toEmail Recipient email address
     * @param file Invoice PDF file
     * @param paymentLink Razorpay payment link
     * @throws MessagingException if email sending fails
     * @throws IOException if file reading fails
     */
    public void sendInvoiceWithPaymentLink(String toEmail, MultipartFile file, String paymentLink) throws MessagingException, IOException {
        // Validate email address
        validationUtil.validateEmail(toEmail);
        
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Your Invoice with Payment Link");
        
        String textBody = "Dear Customer,\n\n" +
                        "Please find attached your invoice.\n\n" +
                        "You can pay online using this link: " + paymentLink + "\n\n" +
                        "Alternatively, you can pay by cash and inform us once the payment is made.\n\n" +
                        "Thank you!";
        helper.setText(textBody);
        
        helper.addAttachment(file.getOriginalFilename(), new ByteArrayResource(file.getBytes()));

        mailSender.send(message);
    }

    /**
     * Sends invoice with payment link (without file attachment)
     * 
     * @param invoice Invoice entity
     * @param paymentLink Razorpay payment link
     */
    public void sendInvoiceWithPaymentLink(Invoice invoice, String paymentLink) {
        try {
            // Try to get email from billing.email first, fallback to phone field
            String toEmail = invoice.getBilling().getEmail();
            if (toEmail == null || toEmail.isEmpty()) {
                toEmail = invoice.getBilling().getPhone();
            }
            
            // Validate email format
            if (toEmail == null || toEmail.isEmpty() || !toEmail.contains("@")) {
                throw new IllegalArgumentException("Valid email address not found in invoice billing details");
            }
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Invoice #" + invoice.getInvoice().getNumber() + " - Payment Link");
            
            String htmlBody = buildInvoiceEmailWithPaymentLink(invoice, paymentLink);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Invoice email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send invoice email: {}", e.getMessage());
            throw new RuntimeException("Failed to send invoice email: " + e.getMessage(), e);
        }
    }

    /**
     * Send payment confirmation email
     * 
     * @param invoice Invoice entity
     */
    public void sendPaymentConfirmation(Invoice invoice) {
        try {
            // Try to get email from billing.email first, fallback to phone field
            String toEmail = invoice.getBilling().getEmail();
            if (toEmail == null || toEmail.isEmpty()) {
                toEmail = invoice.getBilling().getPhone();
            }
            
            // Validate email format
            if (toEmail == null || toEmail.isEmpty() || !toEmail.contains("@")) {
                log.warn("Valid email address not found for payment confirmation. Skipping email.");
                return;
            }
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Payment Received - Invoice #" + invoice.getInvoice().getNumber());
            
            String htmlBody = buildPaymentConfirmationEmail(invoice);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Payment confirmation email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send payment confirmation: {}", e.getMessage());
            throw new RuntimeException("Failed to send payment confirmation: " + e.getMessage(), e);
        }
    }

    /**
     * Build HTML email body for invoice with payment link
     */
    private String buildInvoiceEmailWithPaymentLink(Invoice invoice, String paymentLink) {
        double totalAmount = calculateTotalAmount(invoice);
        
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #2c3e50;">Invoice from %s</h2>
                    
                    <p>Dear %s,</p>
                    
                    <p>Thank you for your business! Please find your invoice details below:</p>
                    
                    <div style="background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <p><strong>Invoice Number:</strong> %s</p>
                        <p><strong>Invoice Date:</strong> %s</p>
                        <p><strong>Due Date:</strong> %s</p>
                        <p><strong>Total Amount:</strong> ₹%.2f</p>
                    </div>
                    
                    <div style="background-color: #e8f5e9; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <h3 style="color: #2e7d32; margin-top: 0;">Pay Online</h3>
                        <p>Click the button below to pay securely online:</p>
                        <a href="%s" style="display: inline-block; background-color: #4CAF50; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; margin: 10px 0;">Pay Now</a>
                        <p style="font-size: 12px; color: #666;">Or copy this link: %s</p>
                    </div>
                    
                    <div style="background-color: #fff3cd; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <h3 style="color: #856404; margin-top: 0;">Alternative Payment Methods</h3>
                        <p>You can also pay by cash or bank transfer. Please inform us once the payment is made.</p>
                    </div>
                    
                    <p>If you have any questions, please don't hesitate to contact us.</p>
                    
                    <p>Best regards,<br>%s</p>
                    
                    <hr style="border: none; border-top: 1px solid #ddd; margin: 30px 0;">
                    <p style="font-size: 12px; color: #666;">This is an automated email. Please do not reply to this email.</p>
                </div>
            </body>
            </html>
            """,
            invoice.getCompany().getName(),
            invoice.getBilling().getName(),
            invoice.getInvoice().getNumber(),
            invoice.getInvoice().getDate(),
            invoice.getInvoice().getDueDate(),
            totalAmount,
            paymentLink,
            paymentLink,
            invoice.getCompany().getName()
        );
    }

    /**
     * Build HTML email body for payment confirmation
     */
    private String buildPaymentConfirmationEmail(Invoice invoice) {
        double totalAmount = calculateTotalAmount(invoice);
        String paymentMethod = invoice.getPaymentDetails() != null ? 
            invoice.getPaymentDetails().getPaymentMethod().toString() : "Unknown";
        
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background-color: #d4edda; padding: 20px; border-radius: 5px; text-align: center;">
                        <h2 style="color: #155724; margin: 0;">✓ Payment Received</h2>
                    </div>
                    
                    <p>Dear %s,</p>
                    
                    <p>We have successfully received your payment. Thank you!</p>
                    
                    <div style="background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <p><strong>Invoice Number:</strong> %s</p>
                        <p><strong>Amount Paid:</strong> ₹%.2f</p>
                        <p><strong>Payment Method:</strong> %s</p>
                        <p><strong>Payment Date:</strong> %s</p>
                    </div>
                    
                    <p>Your invoice has been marked as PAID in our system.</p>
                    
                    <p>Thank you for your business!</p>
                    
                    <p>Best regards,<br>%s</p>
                    
                    <hr style="border: none; border-top: 1px solid #ddd; margin: 30px 0;">
                    <p style="font-size: 12px; color: #666;">This is an automated email. Please do not reply to this email.</p>
                </div>
            </body>
            </html>
            """,
            invoice.getBilling().getName(),
            invoice.getInvoice().getNumber(),
            totalAmount,
            paymentMethod,
            invoice.getPaymentDetails() != null ? invoice.getPaymentDetails().getPaymentDate().toString() : "N/A",
            invoice.getCompany().getName()
        );
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
