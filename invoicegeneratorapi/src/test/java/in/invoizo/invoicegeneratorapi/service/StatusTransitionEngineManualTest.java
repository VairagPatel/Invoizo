package in.invoizo.invoicegeneratorapi.service;

import in.invoizo.invoicegeneratorapi.entity.Invoice.InvoiceStatus;

/**
 * Manual test to verify StatusTransitionEngine functionality.
 * Run this class to verify the state machine rules are working correctly.
 */
public class StatusTransitionEngineManualTest {
    
    public static void main(String[] args) {
        StatusTransitionEngine engine = new StatusTransitionEngine();
        
        System.out.println("=== StatusTransitionEngine Manual Verification ===\n");
        
        // Test allowed transitions (Requirements 7.1-7.8)
        System.out.println("Testing ALLOWED transitions:");
        testTransition(engine, InvoiceStatus.DRAFT, InvoiceStatus.SENT, true, "7.1");
        testTransition(engine, InvoiceStatus.SENT, InvoiceStatus.VIEWED, true, "7.2");
        testTransition(engine, InvoiceStatus.SENT, InvoiceStatus.OVERDUE, true, "7.3");
        testTransition(engine, InvoiceStatus.VIEWED, InvoiceStatus.OVERDUE, true, "7.4");
        testTransition(engine, InvoiceStatus.SENT, InvoiceStatus.PAID, true, "7.5");
        testTransition(engine, InvoiceStatus.VIEWED, InvoiceStatus.PAID, true, "7.6");
        testTransition(engine, InvoiceStatus.OVERDUE, InvoiceStatus.PAID, true, "7.7");
        testTransition(engine, InvoiceStatus.DRAFT, InvoiceStatus.CANCELLED, true, "7.8");
        testTransition(engine, InvoiceStatus.SENT, InvoiceStatus.CANCELLED, true, "7.8");
        testTransition(engine, InvoiceStatus.PAID, InvoiceStatus.CANCELLED, true, "7.8");
        
        System.out.println("\nTesting REJECTED transitions:");
        testTransition(engine, InvoiceStatus.PAID, InvoiceStatus.SENT, false, "7.9");
        testTransition(engine, InvoiceStatus.PAID, InvoiceStatus.VIEWED, false, "7.10");
        testTransition(engine, InvoiceStatus.PAID, InvoiceStatus.OVERDUE, false, "7.11");
        testTransition(engine, InvoiceStatus.DRAFT, InvoiceStatus.VIEWED, false, "N/A");
        testTransition(engine, InvoiceStatus.CANCELLED, InvoiceStatus.SENT, false, "N/A");
        
        System.out.println("\nTesting IDEMPOTENCY (same-state transitions):");
        testTransition(engine, InvoiceStatus.DRAFT, InvoiceStatus.DRAFT, true, "Idempotency");
        testTransition(engine, InvoiceStatus.SENT, InvoiceStatus.SENT, true, "Idempotency");
        testTransition(engine, InvoiceStatus.PAID, InvoiceStatus.PAID, true, "Idempotency");
        
        System.out.println("\nTesting NULL handling:");
        testTransition(engine, null, InvoiceStatus.SENT, false, "Null handling");
        testTransition(engine, InvoiceStatus.DRAFT, null, false, "Null handling");
        
        System.out.println("\nTesting validateTransition method:");
        try {
            engine.validateTransition(InvoiceStatus.DRAFT, InvoiceStatus.SENT);
            System.out.println("✓ validateTransition(DRAFT → SENT) succeeded");
        } catch (Exception e) {
            System.out.println("✗ validateTransition(DRAFT → SENT) failed: " + e.getMessage());
        }
        
        try {
            engine.validateTransition(InvoiceStatus.PAID, InvoiceStatus.SENT);
            System.out.println("✗ validateTransition(PAID → SENT) should have thrown exception");
        } catch (IllegalStateException e) {
            System.out.println("✓ validateTransition(PAID → SENT) correctly threw exception");
        }
        
        System.out.println("\n=== All manual tests completed ===");
    }
    
    private static void testTransition(StatusTransitionEngine engine, 
                                      InvoiceStatus from, 
                                      InvoiceStatus to, 
                                      boolean expected, 
                                      String requirement) {
        boolean result = engine.isTransitionAllowed(from, to);
        String status = (result == expected) ? "✓" : "✗";
        String fromStr = (from == null) ? "null" : from.toString();
        String toStr = (to == null) ? "null" : to.toString();
        System.out.printf("%s %s → %s: %s (Req: %s)%n", 
            status, fromStr, toStr, result ? "ALLOWED" : "REJECTED", requirement);
    }
}
