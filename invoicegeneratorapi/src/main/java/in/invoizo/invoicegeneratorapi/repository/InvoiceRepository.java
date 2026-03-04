package in.invoizo.invoicegeneratorapi.repository;

import in.invoizo.invoicegeneratorapi.entity.Invoice;
import in.invoizo.invoicegeneratorapi.entity.Invoice.InvoiceStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends MongoRepository<Invoice, String> {

    List<Invoice> findByClerkId(String id);

    Optional<Invoice> findByClerkIdAndId(String clerkId, String id);

    List<Invoice> findByStatusIn(List<InvoiceStatus> statuses);

    List<Invoice> findByClerkIdAndStatus(String clerkId, InvoiceStatus status);

    List<Invoice> findByStatus(InvoiceStatus status);

    /**
     * Finds invoices that are overdue.
     * An invoice is overdue if:
     * - Status is SENT or VIEWED
     * - Due date is before the current date
     * 
     * Requirements: 5.2
     * 
     * @param currentDate The current date to compare against
     * @return List of overdue invoices
     */
    @Query("{ 'status': { $in: ['SENT', 'VIEWED'] }, 'invoice.dueDate': { $lt: ?0 } }")
    List<Invoice> findOverdueInvoices(String currentDate);

    /**
     * Finds an invoice by Razorpay payment link ID.
     * Used for webhook lookup to identify which invoice a payment belongs to.
     * 
     * Requirements: 4.2, 9.1
     * 
     * @param razorpayPaymentLinkId The Razorpay payment link ID
     * @return Optional containing the invoice if found
     */
    Optional<Invoice> findByRazorpayPaymentLinkId(String razorpayPaymentLinkId);
}
