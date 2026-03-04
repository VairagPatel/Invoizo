package in.invoizo.invoicegeneratorapi.service;

import in.invoizo.invoicegeneratorapi.entity.Invoice.InvoiceStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Manual verification of Property 11: State Machine Rejects Invalid Transitions
 * 
 * **Validates: Requirements 7.9, 7.10, 7.11**
 * 
 * This demonstrates that the property test logic for rejected transitions is correct
 * by running the same assertions manually across all invalid transitions.
 * 
 * Specifically tests:
 * - Requirement 7.9: PAID → SENT is rejected
 * - Requirement 7.10: PAID → VIEWED is rejected
 * - Requirement 7.11: PAID → OVERDUE is rejected
 */
public class StatusTransitionEngineProperty11TestManual {
    
    private static final StatusTransitionEngine engine = new StatusTransitionEngine();
    private static int passCount = 0;
    private static int failCount = 0;
    
    public static void main(String[] args) {
        System.out.println("=== Property 11: State Machine Rejects Invalid Transitions ===");
        System.out.println("**Validates: Requirements 7.9, 7.10, 7.11**\n");
        
        // Get all invalid transitions
        List<StatusTransition> invalidTransitions = getInvalidTransitions();
        
        System.out.println("Testing " + invalidTransitions.size() + " invalid transitions...\n");
        
        // Test each invalid transition
        for (StatusTransition transition : invalidTransitions) {
            testInvalidTransition(transition);
        }
        
        System.out.println("\n=== Testing PAID Status Specific Rejections ===\n");
        
        // Test PAID transitions (Requirements 7.9, 7.10, 7.11)
        testPaidTransitions();
        
        System.out.println("\n=== Testing CANCELLED Terminal State ===\n");
        
        // Test CANCELLED transitions
        testCancelledTerminalState();
        
        // Print summary
        System.out.println("\n=== Test Summary ===");
        System.out.println("Passed: " + passCount);
        System.out.println("Failed: " + failCount);
        System.out.println("Total:  " + (passCount + failCount));
        
        if (failCount == 0) {
            System.out.println("\n✓ All property tests passed!");
            System.out.println("Property 11 is verified: State Machine Rejects Invalid Transitions");
            System.out.println("Requirements 7.9, 7.10, 7.11 are satisfied");
        } else {
            System.out.println("\n✗ Some tests failed. Please review the output above.");
        }
    }
    
    private static void testInvalidTransition(StatusTransition transition) {
        boolean isAllowed = engine.isTransitionAllowed(transition.from, transition.to);
        
        if (!isAllowed) {
            pass(String.format("%s → %s is correctly rejected", transition.from, transition.to));
            
            // Also test that validateTransition throws IllegalStateException
            try {
                engine.validateTransition(transition.from, transition.to);
                fail(String.format("  validateTransition(%s → %s) should throw but didn't", 
                    transition.from, transition.to));
            } catch (IllegalStateException e) {
                // Verify exception message is informative
                String message = e.getMessage();
                if (message.contains("Invalid status transition") &&
                    message.contains(transition.from.toString()) &&
                    message.contains(transition.to.toString())) {
                    pass(String.format("  validateTransition(%s → %s) throws with informative message", 
                        transition.from, transition.to));
                } else {
                    fail(String.format("  validateTransition(%s → %s) exception message not informative: %s", 
                        transition.from, transition.to, message));
                }
            } catch (Exception e) {
                fail(String.format("  validateTransition(%s → %s) threw wrong exception type: %s", 
                    transition.from, transition.to, e.getClass().getName()));
            }
        } else {
            fail(String.format("%s → %s should be rejected but was allowed", 
                transition.from, transition.to));
        }
    }
    
    private static void testPaidTransitions() {
        System.out.println("Testing PAID transitions (Req 7.9, 7.10, 7.11):");
        
        for (InvoiceStatus target : InvoiceStatus.values()) {
            boolean isAllowed = engine.isTransitionAllowed(InvoiceStatus.PAID, target);
            boolean shouldBeAllowed = target == InvoiceStatus.CANCELLED
                    || target == InvoiceStatus.PAID;
            
            if (isAllowed == shouldBeAllowed) {
                pass(String.format("  PAID → %s: %s (correct)", 
                    target, isAllowed ? "allowed" : "rejected"));
            } else {
                fail(String.format("  PAID → %s: expected %s but got %s", 
                    target, shouldBeAllowed ? "allowed" : "rejected", 
                    isAllowed ? "allowed" : "rejected"));
            }
            
            // Specifically verify the three forbidden transitions
            if (target == InvoiceStatus.SENT) {
                if (!isAllowed) {
                    pass("  ✓ Requirement 7.9: PAID → SENT is rejected");
                } else {
                    fail("  ✗ Requirement 7.9: PAID → SENT should be rejected");
                }
            } else if (target == InvoiceStatus.VIEWED) {
                if (!isAllowed) {
                    pass("  ✓ Requirement 7.10: PAID → VIEWED is rejected");
                } else {
                    fail("  ✗ Requirement 7.10: PAID → VIEWED should be rejected");
                }
            } else if (target == InvoiceStatus.OVERDUE) {
                if (!isAllowed) {
                    pass("  ✓ Requirement 7.11: PAID → OVERDUE is rejected");
                } else {
                    fail("  ✗ Requirement 7.11: PAID → OVERDUE should be rejected");
                }
            }
        }
    }
    
    private static void testCancelledTerminalState() {
        System.out.println("Testing CANCELLED terminal state:");
        
        for (InvoiceStatus target : InvoiceStatus.values()) {
            boolean isAllowed = engine.isTransitionAllowed(InvoiceStatus.CANCELLED, target);
            boolean shouldBeAllowed = target == InvoiceStatus.CANCELLED;
            
            if (isAllowed == shouldBeAllowed) {
                pass(String.format("  CANCELLED → %s: %s (correct)", 
                    target, isAllowed ? "allowed" : "rejected"));
            } else {
                fail(String.format("  CANCELLED → %s: expected %s but got %s", 
                    target, shouldBeAllowed ? "allowed" : "rejected", 
                    isAllowed ? "allowed" : "rejected"));
            }
        }
    }
    
    private static List<StatusTransition> getInvalidTransitions() {
        List<StatusTransition> transitions = new ArrayList<>();
        
        // DRAFT invalid transitions
        transitions.add(new StatusTransition(InvoiceStatus.DRAFT, InvoiceStatus.VIEWED));
        transitions.add(new StatusTransition(InvoiceStatus.DRAFT, InvoiceStatus.PAID));
        transitions.add(new StatusTransition(InvoiceStatus.DRAFT, InvoiceStatus.OVERDUE));
        
        // SENT invalid transitions
        transitions.add(new StatusTransition(InvoiceStatus.SENT, InvoiceStatus.DRAFT));
        
        // VIEWED invalid transitions
        transitions.add(new StatusTransition(InvoiceStatus.VIEWED, InvoiceStatus.DRAFT));
        transitions.add(new StatusTransition(InvoiceStatus.VIEWED, InvoiceStatus.SENT));
        
        // PAID invalid transitions (Requirements 7.9, 7.10, 7.11)
        transitions.add(new StatusTransition(InvoiceStatus.PAID, InvoiceStatus.DRAFT));
        transitions.add(new StatusTransition(InvoiceStatus.PAID, InvoiceStatus.SENT));    // Requirement 7.9
        transitions.add(new StatusTransition(InvoiceStatus.PAID, InvoiceStatus.VIEWED));  // Requirement 7.10
        transitions.add(new StatusTransition(InvoiceStatus.PAID, InvoiceStatus.OVERDUE)); // Requirement 7.11
        
        // OVERDUE invalid transitions
        transitions.add(new StatusTransition(InvoiceStatus.OVERDUE, InvoiceStatus.DRAFT));
        transitions.add(new StatusTransition(InvoiceStatus.OVERDUE, InvoiceStatus.SENT));
        transitions.add(new StatusTransition(InvoiceStatus.OVERDUE, InvoiceStatus.VIEWED));
        
        // CANCELLED invalid transitions (terminal state)
        transitions.add(new StatusTransition(InvoiceStatus.CANCELLED, InvoiceStatus.DRAFT));
        transitions.add(new StatusTransition(InvoiceStatus.CANCELLED, InvoiceStatus.SENT));
        transitions.add(new StatusTransition(InvoiceStatus.CANCELLED, InvoiceStatus.VIEWED));
        transitions.add(new StatusTransition(InvoiceStatus.CANCELLED, InvoiceStatus.PAID));
        transitions.add(new StatusTransition(InvoiceStatus.CANCELLED, InvoiceStatus.OVERDUE));
        
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
