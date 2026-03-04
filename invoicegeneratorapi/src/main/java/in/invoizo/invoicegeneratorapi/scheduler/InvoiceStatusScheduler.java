package in.invoizo.invoicegeneratorapi.scheduler;

import in.invoizo.invoicegeneratorapi.entity.Invoice;
import in.invoizo.invoicegeneratorapi.entity.Invoice.InvoiceStatus;
import in.invoizo.invoicegeneratorapi.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceStatusScheduler {

    private final InvoiceRepository invoiceRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Scheduled task to detect and update overdue invoices
     * Runs every hour
     */
    @Scheduled(cron = "0 0 * * * *")
    public void updateOverdueInvoices() {
        log.info("Starting overdue invoice detection task");
        
        LocalDate today = LocalDate.now();
        
        // Find all invoices with SENT or VIEWED status
        List<Invoice> invoices = invoiceRepository.findByStatusIn(
                List.of(InvoiceStatus.SENT, InvoiceStatus.VIEWED)
        );
        
        int updatedCount = 0;
        
        for (Invoice invoice : invoices) {
            if (checkAndUpdateOverdue(invoice, today)) {
                updatedCount++;
            }
        }
        
        log.info("Overdue invoice detection completed. Updated {} invoices to OVERDUE status", 
                updatedCount);
    }
    
    /**
     * Check if an invoice is overdue and update its status
     * 
     * @param invoice Invoice to check
     * @param today Current date
     * @return true if invoice was updated to OVERDUE
     */
    public boolean checkAndUpdateOverdue(Invoice invoice, LocalDate today) {
        try {
            String dueDateStr = invoice.getInvoice().getDueDate();
            if (dueDateStr != null && !dueDateStr.isEmpty()) {
                LocalDate dueDate = LocalDate.parse(dueDateStr, DATE_FORMATTER);
                
                // If due date is in the past, mark as overdue
                if (dueDate.isBefore(today)) {
                    invoice.setStatus(InvoiceStatus.OVERDUE);
                    invoiceRepository.save(invoice);
                    log.debug("Marked invoice {} as OVERDUE (due date: {})", 
                            invoice.getId(), dueDateStr);
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("Error processing invoice {} for overdue detection: {}", 
                    invoice.getId(), e.getMessage());
        }
        return false;
    }
    
    /**
     * Check a single invoice for overdue status
     * Can be called on-demand when retrieving invoices
     * 
     * @param invoice Invoice to check
     * @return true if invoice was updated to OVERDUE
     */
    public boolean checkInvoiceOverdue(Invoice invoice) {
        if (invoice.getStatus() == InvoiceStatus.SENT || 
            invoice.getStatus() == InvoiceStatus.VIEWED) {
            return checkAndUpdateOverdue(invoice, LocalDate.now());
        }
        return false;
    }
}
