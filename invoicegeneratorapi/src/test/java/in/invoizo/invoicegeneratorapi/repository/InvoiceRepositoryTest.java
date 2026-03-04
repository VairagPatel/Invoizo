package in.invoizo.invoicegeneratorapi.repository;

import in.invoizo.invoicegeneratorapi.entity.Invoice;
import in.invoizo.invoicegeneratorapi.entity.Invoice.InvoiceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InvoiceRepository query methods
 * Tests the new query methods added for invoice-auto-status-sync feature
 */
@DataMongoTest
class InvoiceRepositoryTest {

    @Autowired
    private InvoiceRepository invoiceRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @BeforeEach
    void setUp() {
        invoiceRepository.deleteAll();
    }

    /**
     * Test findOverdueInvoices method
     * Requirements: 5.2
     */
    @Test
    void testFindOverdueInvoices_shouldReturnSentAndViewedInvoicesWithPastDueDate() {
        // Given: Create test invoices with various statuses and due dates
        LocalDate today = LocalDate.now();
        String todayStr = today.format(DATE_FORMATTER);
        String pastDateStr = today.minusDays(5).format(DATE_FORMATTER);
        String futureDateStr = today.plusDays(5).format(DATE_FORMATTER);

        // Overdue SENT invoice
        Invoice overdueSent = createTestInvoice("1", "clerk1", InvoiceStatus.SENT, pastDateStr);
        invoiceRepository.save(overdueSent);

        // Overdue VIEWED invoice
        Invoice overdueViewed = createTestInvoice("2", "clerk1", InvoiceStatus.VIEWED, pastDateStr);
        invoiceRepository.save(overdueViewed);

        // Future SENT invoice (not overdue)
        Invoice futureSent = createTestInvoice("3", "clerk1", InvoiceStatus.SENT, futureDateStr);
        invoiceRepository.save(futureSent);

        // PAID invoice with past due date (should not be returned)
        Invoice paidPast = createTestInvoice("4", "clerk1", InvoiceStatus.PAID, pastDateStr);
        invoiceRepository.save(paidPast);

        // DRAFT invoice with past due date (should not be returned)
        Invoice draftPast = createTestInvoice("5", "clerk1", InvoiceStatus.DRAFT, pastDateStr);
        invoiceRepository.save(draftPast);

        // When: Query for overdue invoices
        List<Invoice> overdueInvoices = invoiceRepository.findOverdueInvoices(todayStr);

        // Then: Should return only SENT and VIEWED invoices with past due dates
        assertEquals(2, overdueInvoices.size(), "Should find exactly 2 overdue invoices");
        
        assertTrue(overdueInvoices.stream()
                .anyMatch(inv -> inv.getId().equals("1") && inv.getStatus() == InvoiceStatus.SENT),
                "Should include overdue SENT invoice");
        
        assertTrue(overdueInvoices.stream()
                .anyMatch(inv -> inv.getId().equals("2") && inv.getStatus() == InvoiceStatus.VIEWED),
                "Should include overdue VIEWED invoice");
    }

    /**
     * Test findByRazorpayPaymentLinkId method
     * Requirements: 4.2, 9.1
     */
    @Test
    void testFindByRazorpayPaymentLinkId_shouldReturnInvoiceWithMatchingPaymentLinkId() {
        // Given: Create invoices with different payment link IDs
        Invoice invoice1 = createTestInvoice("1", "clerk1", InvoiceStatus.SENT, 
                LocalDate.now().plusDays(30).format(DATE_FORMATTER));
        invoice1.setRazorpayPaymentLinkId("plink_ABC123");
        invoiceRepository.save(invoice1);

        Invoice invoice2 = createTestInvoice("2", "clerk1", InvoiceStatus.SENT, 
                LocalDate.now().plusDays(30).format(DATE_FORMATTER));
        invoice2.setRazorpayPaymentLinkId("plink_XYZ789");
        invoiceRepository.save(invoice2);

        // When: Query by payment link ID
        Optional<Invoice> found = invoiceRepository.findByRazorpayPaymentLinkId("plink_ABC123");

        // Then: Should return the correct invoice
        assertTrue(found.isPresent(), "Should find invoice with matching payment link ID");
        assertEquals("1", found.get().getId(), "Should return invoice with ID 1");
        assertEquals("plink_ABC123", found.get().getRazorpayPaymentLinkId(), 
                "Should have correct payment link ID");
    }

    /**
     * Test findByRazorpayPaymentLinkId with non-existent ID
     */
    @Test
    void testFindByRazorpayPaymentLinkId_shouldReturnEmptyForNonExistentId() {
        // Given: Create an invoice with a payment link ID
        Invoice invoice = createTestInvoice("1", "clerk1", InvoiceStatus.SENT, 
                LocalDate.now().plusDays(30).format(DATE_FORMATTER));
        invoice.setRazorpayPaymentLinkId("plink_ABC123");
        invoiceRepository.save(invoice);

        // When: Query with non-existent payment link ID
        Optional<Invoice> found = invoiceRepository.findByRazorpayPaymentLinkId("plink_NONEXISTENT");

        // Then: Should return empty
        assertFalse(found.isPresent(), "Should not find invoice with non-existent payment link ID");
    }

    /**
     * Test findByStatus method (existing method, verifying it still works)
     * Requirements: 9.1
     */
    @Test
    void testFindByStatus_shouldReturnInvoicesWithMatchingStatus() {
        // Given: Create invoices with different statuses
        Invoice sent1 = createTestInvoice("1", "clerk1", InvoiceStatus.SENT, 
                LocalDate.now().plusDays(30).format(DATE_FORMATTER));
        invoiceRepository.save(sent1);

        Invoice sent2 = createTestInvoice("2", "clerk1", InvoiceStatus.SENT, 
                LocalDate.now().plusDays(30).format(DATE_FORMATTER));
        invoiceRepository.save(sent2);

        Invoice draft = createTestInvoice("3", "clerk1", InvoiceStatus.DRAFT, 
                LocalDate.now().plusDays(30).format(DATE_FORMATTER));
        invoiceRepository.save(draft);

        // When: Query by status
        List<Invoice> sentInvoices = invoiceRepository.findByStatus(InvoiceStatus.SENT);

        // Then: Should return only SENT invoices
        assertEquals(2, sentInvoices.size(), "Should find exactly 2 SENT invoices");
        assertTrue(sentInvoices.stream().allMatch(inv -> inv.getStatus() == InvoiceStatus.SENT),
                "All returned invoices should have SENT status");
    }

    /**
     * Helper method to create a test invoice
     */
    private Invoice createTestInvoice(String id, String clerkId, InvoiceStatus status, String dueDate) {
        Invoice invoice = new Invoice();
        invoice.setId(id);
        invoice.setClerkId(clerkId);
        invoice.setStatus(status);

        Invoice.InvoiceDetails details = new Invoice.InvoiceDetails();
        details.setNumber("INV-" + id);
        details.setDate(LocalDate.now().format(DATE_FORMATTER));
        details.setDueDate(dueDate);
        invoice.setInvoice(details);

        return invoice;
    }
}
