package in.invoizo.invoicegeneratorapi.service;

import in.invoizo.invoicegeneratorapi.entity.Invoice.InvoiceStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Manual verification of property-based test logic for StatusTransitionEngine.
 * This demonstrates that the property test logic is correct by running the same
 * assertions manually across all valid transitions.
 * 
 * Run this class to verify Property 10: State Machine Allows Valid Transitions
 */
public class StatusTransitionEnginePropertyTestManual {
    
    private static final StatusTransitionEngine engine = new StatusTransitionEngine();
    private static int passCount = 0;
    private static int failCount = 0;
    
    public static void main(String[] args) {
        System.out.println("=== Property 10: State Machine Allows Valid Transitions ===");
        System.out.println("**Validates: Requirements 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8**\n");
        
        // Get all valid transitions as defined in the property test
        List<StatusTransition> validTransitions = getValidTransitions();
        
        System.out.println("Testing " + validTransitions.size() + " valid transitions...\n");
        
        // Test each valid transition
        for (StatusTransition transition : validTransitions) {
            testValidTransition(transition);
        }
        
        System.out.println("\n=== Testing Same-State Transitions (Idempotency) ===\n");
        
        // Test idempotency for all statuses
        for (InvoiceStatus status : InvoiceStatus.values()) {
            testSameStateTransition(status);
        }
        
        System.out.println("\n=== Testing Status-Specific Transition Rules ===\n");
        
        // Test DRAFT transitions
        testDraftTransitions();
        
        // Test SENT transitions
        testSentTransitions();
        
        // Test VIEWED transitions
        testViewedTransitions();
        
        // Test OVERDUE transitions
        testOverdueTransitions();
        
        // Test CANCELLED transitions (any status → CANCELLED)
        testCancelledTransitions();
        
        // Test consistency between isTransitionAllowed and validateTransition
        System.out.println("\n=== Testing Consistency Between Methods ===\n");
        testMethodConsistency();
        
        // Print summary
        System.out.println("\n=== Test Summary ===");
        System.out.println("Passed: " + passCount);
        System.out.println("Failed: " + failCount);
        System.out.println("Total:  " + (passCount + failCount));
        
        if (failCount == 0) {
            System.out.println("\n✓ All property tests passed!");
            System.out.println("Property 10 is verified: State Machine Allows Valid Transitions");
        } else {
            System.out.println("\n✗ Some tests failed. Please review the output above.");
        }
    }
    
    private static void testValidTransition(StatusTransition transition) {
        boolean isAllowed = engine.isTransitionAllowed(transition.from, transition.to);
        
        if (isAllowed) {
            pass(String.format("%s → %s is allowed", transition.from, transition.to));
            
            // Also test that validateTransition doesn't throw
            try {
                engine.validateTransition(transition.from, transition.to);
                pass(String.format("  validateTransition(%s → %s) does not throw", 
                    transition.from, transition.to));
            } catch (Exception e) {
                fail(String.format("  validateTransition(%s → %s) threw exception: %s", 
                    transition.from, transition.to, e.getMessage()));
            }
        } else {
            fail(String.format("%s → %s should be allowed but was rejected", 
                transition.from, transition.to));
        }
    }
    
    private static void testSameStateTransition(InvoiceStatus status) {
        boolean isAllowed = engine.isTransitionAllowed(status, status);
        
        if (isAllowed) {
            pass(String.format("%s → %s (same-state) is allowed for idempotency", status, status));
        } else {
            fail(String.format("%s → %s (same-state) should be allowed for idempotency", 
                status, status));
        }
    }
    
    private static void testDraftTransitions() {
        System.out.println("Testing DRAFT transitions (Req 7.1, 7.8):");
        
        for (InvoiceStatus target : InvoiceStatus.values()) {
            boolean isAllowed = engine.isTransitionAllowed(InvoiceStatus.DRAFT, target);
            boolean shouldBeAllowed = target == InvoiceStatus.SENT 
                    || target == InvoiceStatus.CANCELLED
                    || target == InvoiceStatus.DRAFT;
            
            if (isAllowed == shouldBeAllowed) {
                pass(String.format("  DRAFT → %s: %s (correct)", 
                    target, isAllowed ? "allowed" : "rejected"));
            } else {
                fail(String.format("  DRAFT → %s: expected %s but got %s", 
                    target, shouldBeAllowed ? "allowed" : "rejected", 
                    isAllowed ? "allowed" : "rejected"));
            }
        }
    }
    
    private static void testSentTransitions() {
        System.out.println("\nTesting SENT transitions (Req 7.2, 7.3, 7.5, 7.8):");
        
        for (InvoiceStatus target : InvoiceStatus.values()) {
            boolean isAllowed = engine.isTransitionAllowed(InvoiceStatus.SENT, target);
            boolean shouldBeAllowed = target == InvoiceStatus.VIEWED
                    || target == InvoiceStatus.PAID
                    || target == InvoiceStatus.OVERDUE
                    || target == InvoiceStatus.CANCELLED
                    || target == InvoiceStatus.SENT;
            
            if (isAllowed == shouldBeAllowed) {
                pass(String.format("  SENT → %s: %s (correct)", 
                    target, isAllowed ? "allowed" : "rejected"));
            } else {
                fail(String.format("  SENT → %s: expected %s but got %s", 
                    target, shouldBeAllowed ? "allowed" : "rejected", 
                    isAllowed ? "allowed" : "rejected"));
            }
        }
    }
    
    private static void testViewedTransitions() {
        System.out.println("\nTesting VIEWED transitions (Req 7.4, 7.6, 7.8):");
        
        for (InvoiceStatus target : InvoiceStatus.values()) {
            boolean isAllowed = engine.isTransitionAllowed(InvoiceStatus.VIEWED, target);
            boolean shouldBeAllowed = target == InvoiceStatus.PAID
                    || target == InvoiceStatus.OVERDUE
                    || target == InvoiceStatus.CANCELLED
                    || target == InvoiceStatus.VIEWED;
            
            if (isAllowed == shouldBeAllowed) {
                pass(String.format("  VIEWED → %s: %s (correct)", 
                    target, isAllowed ? "allowed" : "rejected"));
            } else {
                fail(String.format("  VIEWED → %s: expected %s but got %s", 
                    target, shouldBeAllowed ? "allowed" : "rejected", 
                    isAllowed ? "allowed" : "rejected"));
            }
        }
    }
    
    private static void testOverdueTransitions() {
        System.out.println("\nTesting OVERDUE transitions (Req 7.7, 7.8):");
        
        for (InvoiceStatus target : InvoiceStatus.values()) {
            boolean isAllowed = engine.isTransitionAllowed(InvoiceStatus.OVERDUE, target);
            boolean shouldBeAllowed = target == InvoiceStatus.PAID
                    || target == InvoiceStatus.CANCELLED
                    || target == InvoiceStatus.OVERDUE;
            
            if (isAllowed == shouldBeAllowed) {
                pass(String.format("  OVERDUE → %s: %s (correct)", 
                    target, isAllowed ? "allowed" : "rejected"));
            } else {
                fail(String.format("  OVERDUE → %s: expected %s but got %s", 
                    target, shouldBeAllowed ? "allowed" : "rejected", 
                    isAllowed ? "allowed" : "rejected"));
            }
        }
    }
    
    private static void testCancelledTransitions() {
        System.out.println("\nTesting any status → CANCELLED (Req 7.8):");
        
        for (InvoiceStatus from : InvoiceStatus.values()) {
            boolean isAllowed = engine.isTransitionAllowed(from, InvoiceStatus.CANCELLED);
            
            if (isAllowed) {
                pass(String.format("  %s → CANCELLED: allowed (correct)", from));
            } else {
                fail(String.format("  %s → CANCELLED: should be allowed (Req 7.8)", from));
            }
        }
    }
    
    private static void testMethodConsistency() {
        int tested = 0;
        int consistent = 0;
        
        for (InvoiceStatus from : InvoiceStatus.values()) {
            for (InvoiceStatus to : InvoiceStatus.values()) {
                tested++;
                boolean isAllowed = engine.isTransitionAllowed(from, to);
                
                try {
                    engine.validateTransition(from, to);
                    // No exception thrown
                    if (isAllowed) {
                        consistent++;
                    } else {
                        fail(String.format("  %s → %s: isTransitionAllowed=false but validateTransition didn't throw", 
                            from, to));
                    }
                } catch (IllegalStateException e) {
                    // Exception thrown
                    if (!isAllowed) {
                        consistent++;
                    } else {
                        fail(String.format("  %s → %s: isTransitionAllowed=true but validateTransition threw exception", 
                            from, to));
                    }
                }
            }
        }
        
        System.out.println(String.format("Tested %d transition combinations", tested));
        System.out.println(String.format("Consistent: %d/%d", consistent, tested));
        
        if (consistent == tested) {
            pass("All transitions are consistent between isTransitionAllowed and validateTransition");
        }
    }
    
    private static List<StatusTransition> getValidTransitions() {
        List<StatusTransition> transitions = new ArrayList<>();
        
        // DRAFT transitions (Requirement 7.1, 7.8)
        transitions.add(new StatusTransition(InvoiceStatus.DRAFT, InvoiceStatus.SENT));
        transitions.add(new StatusTransition(InvoiceStatus.DRAFT, InvoiceStatus.CANCELLED));
        
        // SENT transitions (Requirements 7.2, 7.3, 7.5, 7.8)
        transitions.add(new StatusTransition(InvoiceStatus.SENT, InvoiceStatus.VIEWED));
        transitions.add(new StatusTransition(InvoiceStatus.SENT, InvoiceStatus.PAID));
        transitions.add(new StatusTransition(InvoiceStatus.SENT, InvoiceStatus.OVERDUE));
        transitions.add(new StatusTransition(InvoiceStatus.SENT, InvoiceStatus.CANCELLED));
        
        // VIEWED transitions (Requirements 7.4, 7.6, 7.8)
        transitions.add(new StatusTransition(InvoiceStatus.VIEWED, InvoiceStatus.PAID));
        transitions.add(new StatusTransition(InvoiceStatus.VIEWED, InvoiceStatus.OVERDUE));
        transitions.add(new StatusTransition(InvoiceStatus.VIEWED, InvoiceStatus.CANCELLED));
        
        // OVERDUE transitions (Requirements 7.7, 7.8)
        transitions.add(new StatusTransition(InvoiceStatus.OVERDUE, InvoiceStatus.PAID));
        transitions.add(new StatusTransition(InvoiceStatus.OVERDUE, InvoiceStatus.CANCELLED));
        
        // PAID transitions (Requirement 7.8)
        transitions.add(new StatusTransition(InvoiceStatus.PAID, InvoiceStatus.CANCELLED));
        
        return transitions;
    }
    
    private static void pass(String message) {
        System.out.println("✓ " + message);
        passCount++;
    }
    
    private static void fail(String message) {
        System.out.println("✗ " + message);
        failCount++;
    }
    
    static class StatusTransition {
        final InvoiceStatus from;
        final InvoiceStatus to;
        
        StatusTransition(InvoiceStatus from, InvoiceStatus to) {
            this.from = from;
            this.to = to;
        }
    }
}
