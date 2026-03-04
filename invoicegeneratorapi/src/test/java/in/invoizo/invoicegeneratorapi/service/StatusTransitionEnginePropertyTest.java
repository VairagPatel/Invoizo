package in.invoizo.invoicegeneratorapi.service;

import in.invoizo.invoicegeneratorapi.entity.Invoice.InvoiceStatus;
import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for StatusTransitionEngine.
 * Tests the state machine rules using property-based testing to verify
 * correctness across all possible status combinations.
 * 
 * Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8
 */
@Tag("Feature: invoice-auto-status-sync, Property 10: State Machine Allows Valid Transitions")
class StatusTransitionEnginePropertyTest {
    
    private final StatusTransitionEngine engine = new StatusTransitionEngine();
    
    /**
     * Property 10: State Machine Allows Valid Transitions
     * 
     * **Validates: Requirements 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8**
     * 
     * For any invoice, the following status transitions should be allowed by the state machine:
     * - DRAFT → SENT (Requirement 7.1)
     * - SENT → VIEWED, PAID, OVERDUE (Requirements 7.2, 7.5, 7.3)
     * - VIEWED → PAID, OVERDUE (Requirements 7.6, 7.4)
     * - OVERDUE → PAID (Requirement 7.7)
     * - Any status → CANCELLED (Requirement 7.8)
     * 
     * This property test verifies that all valid transitions defined in the design
     * document are correctly allowed by the StatusTransitionEngine.
     */
    @Property(tries = 100)
    void stateTransitionEngineAllowsValidTransitions(
            @ForAll @From("validTransitions") StatusTransition transition) {
        
        // When checking if a valid transition is allowed
        boolean isAllowed = engine.isTransitionAllowed(transition.from, transition.to);
        
        // Then the transition should be allowed
        assertTrue(isAllowed,
                String.format("Valid transition %s → %s should be allowed", 
                    transition.from, transition.to));
        
        // And validateTransition should not throw an exception
        assertDoesNotThrow(() -> engine.validateTransition(transition.from, transition.to),
                String.format("Valid transition %s → %s should not throw exception", 
                    transition.from, transition.to));
    }
    
    /**
     * Property: Same-state transitions are allowed for idempotency
     * 
     * For any status, transitioning to the same status should be allowed.
     * This supports idempotent operations like repeated webhook deliveries.
     */
    @Property(tries = 100)
    void sameStateTransitionsAreAllowedForIdempotency(
            @ForAll InvoiceStatus status) {
        
        // When checking if a same-state transition is allowed
        boolean isAllowed = engine.isTransitionAllowed(status, status);
        
        // Then the transition should be allowed
        assertTrue(isAllowed,
                String.format("Same-state transition %s → %s should be allowed for idempotency", 
                    status, status));
    }
    
    /**
     * Property: DRAFT can only transition to SENT or CANCELLED
     * 
     * **Validates: Requirement 7.1, 7.8**
     * 
     * Verifies that DRAFT status only allows transitions to SENT or CANCELLED,
     * and rejects all other transitions.
     */
    @Property(tries = 100)
    void draftOnlyTransitionsToSentOrCancelled(
            @ForAll InvoiceStatus targetStatus) {
        
        // When checking transitions from DRAFT
        boolean isAllowed = engine.isTransitionAllowed(InvoiceStatus.DRAFT, targetStatus);
        
        // Then only SENT, CANCELLED, and DRAFT (idempotency) should be allowed
        boolean shouldBeAllowed = targetStatus == InvoiceStatus.SENT 
                || targetStatus == InvoiceStatus.CANCELLED
                || targetStatus == InvoiceStatus.DRAFT;
        
        assertEquals(shouldBeAllowed, isAllowed,
                String.format("DRAFT → %s should be %s", 
                    targetStatus, shouldBeAllowed ? "allowed" : "rejected"));
    }
    
    /**
     * Property: SENT can transition to VIEWED, PAID, OVERDUE, or CANCELLED
     * 
     * **Validates: Requirements 7.2, 7.3, 7.5, 7.8**
     * 
     * Verifies that SENT status allows transitions to VIEWED, PAID, OVERDUE, or CANCELLED,
     * and rejects all other transitions.
     */
    @Property(tries = 100)
    void sentTransitionsToViewedPaidOverdueOrCancelled(
            @ForAll InvoiceStatus targetStatus) {
        
        // When checking transitions from SENT
        boolean isAllowed = engine.isTransitionAllowed(InvoiceStatus.SENT, targetStatus);
        
        // Then only VIEWED, PAID, OVERDUE, CANCELLED, and SENT (idempotency) should be allowed
        boolean shouldBeAllowed = targetStatus == InvoiceStatus.VIEWED
                || targetStatus == InvoiceStatus.PAID
                || targetStatus == InvoiceStatus.OVERDUE
                || targetStatus == InvoiceStatus.CANCELLED
                || targetStatus == InvoiceStatus.SENT;
        
        assertEquals(shouldBeAllowed, isAllowed,
                String.format("SENT → %s should be %s", 
                    targetStatus, shouldBeAllowed ? "allowed" : "rejected"));
    }
    
    /**
     * Property: VIEWED can transition to PAID, OVERDUE, or CANCELLED
     * 
     * **Validates: Requirements 7.4, 7.6, 7.8**
     * 
     * Verifies that VIEWED status allows transitions to PAID, OVERDUE, or CANCELLED,
     * and rejects all other transitions.
     */
    @Property(tries = 100)
    void viewedTransitionsToPaidOverdueOrCancelled(
            @ForAll InvoiceStatus targetStatus) {
        
        // When checking transitions from VIEWED
        boolean isAllowed = engine.isTransitionAllowed(InvoiceStatus.VIEWED, targetStatus);
        
        // Then only PAID, OVERDUE, CANCELLED, and VIEWED (idempotency) should be allowed
        boolean shouldBeAllowed = targetStatus == InvoiceStatus.PAID
                || targetStatus == InvoiceStatus.OVERDUE
                || targetStatus == InvoiceStatus.CANCELLED
                || targetStatus == InvoiceStatus.VIEWED;
        
        assertEquals(shouldBeAllowed, isAllowed,
                String.format("VIEWED → %s should be %s", 
                    targetStatus, shouldBeAllowed ? "allowed" : "rejected"));
    }
    
    /**
     * Property: OVERDUE can transition to PAID or CANCELLED
     * 
     * **Validates: Requirements 7.7, 7.8**
     * 
     * Verifies that OVERDUE status allows transitions to PAID or CANCELLED,
     * and rejects all other transitions.
     */
    @Property(tries = 100)
    void overdueTransitionsToPaidOrCancelled(
            @ForAll InvoiceStatus targetStatus) {
        
        // When checking transitions from OVERDUE
        boolean isAllowed = engine.isTransitionAllowed(InvoiceStatus.OVERDUE, targetStatus);
        
        // Then only PAID, CANCELLED, and OVERDUE (idempotency) should be allowed
        boolean shouldBeAllowed = targetStatus == InvoiceStatus.PAID
                || targetStatus == InvoiceStatus.CANCELLED
                || targetStatus == InvoiceStatus.OVERDUE;
        
        assertEquals(shouldBeAllowed, isAllowed,
                String.format("OVERDUE → %s should be %s", 
                    targetStatus, shouldBeAllowed ? "allowed" : "rejected"));
    }
    
    /**
     * Property: Any status can transition to CANCELLED
     * 
     * **Validates: Requirement 7.8**
     * 
     * Verifies that all statuses allow transition to CANCELLED,
     * supporting manual cancellation from any state.
     */
    @Property(tries = 100)
    void anyStatusCanTransitionToCancelled(
            @ForAll InvoiceStatus fromStatus) {
        
        // When checking transition to CANCELLED from any status
        boolean isAllowed = engine.isTransitionAllowed(fromStatus, InvoiceStatus.CANCELLED);
        
        // Then the transition should always be allowed
        assertTrue(isAllowed,
                String.format("%s → CANCELLED should be allowed (Requirement 7.8)", fromStatus));
    }
    
    /**
     * Property 11: State Machine Rejects Invalid Transitions
     * 
     * **Validates: Requirements 7.9, 7.10, 7.11**
     * 
     * For any invoice with status PAID, the system should reject transitions to SENT, VIEWED, or OVERDUE.
     * Similarly, CANCELLED invoices should reject transitions to any status except CANCELLED.
     * 
     * This property test verifies that all invalid transitions defined in the design
     * document are correctly rejected by the StatusTransitionEngine.
     * 
     * Specifically tests:
     * - Requirement 7.9: PAID → SENT is rejected
     * - Requirement 7.10: PAID → VIEWED is rejected
     * - Requirement 7.11: PAID → OVERDUE is rejected
     */
    @Property(tries = 100)
    @Tag("Feature: invoice-auto-status-sync, Property 11: State Machine Rejects Invalid Transitions")
    void stateTransitionEngineRejectsInvalidTransitions(
            @ForAll @From("invalidTransitions") StatusTransition transition) {
        
        // When checking if an invalid transition is allowed
        boolean isAllowed = engine.isTransitionAllowed(transition.from, transition.to);
        
        // Then the transition should be rejected
        assertFalse(isAllowed,
                String.format("Invalid transition %s → %s should be rejected", 
                    transition.from, transition.to));
        
        // And validateTransition should throw an IllegalStateException
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> engine.validateTransition(transition.from, transition.to),
                String.format("Invalid transition %s → %s should throw exception", 
                    transition.from, transition.to));
        
        // And the exception message should be informative
        assertTrue(exception.getMessage().contains("Invalid status transition"),
                "Exception message should indicate invalid transition");
        assertTrue(exception.getMessage().contains(transition.from.toString()),
                "Exception message should contain source status");
        assertTrue(exception.getMessage().contains(transition.to.toString()),
                "Exception message should contain target status");
    }
    
    /**
     * Property: PAID status rejects transitions to SENT, VIEWED, and OVERDUE
     * 
     * **Validates: Requirements 7.9, 7.10, 7.11**
     * 
     * Verifies that PAID status specifically rejects the three forbidden transitions:
     * - PAID → SENT (Requirement 7.9)
     * - PAID → VIEWED (Requirement 7.10)
     * - PAID → OVERDUE (Requirement 7.11)
     */
    @Property(tries = 100)
    void paidStatusRejectsTransitionsToSentViewedAndOverdue(
            @ForAll InvoiceStatus targetStatus) {
        
        // When checking transitions from PAID
        boolean isAllowed = engine.isTransitionAllowed(InvoiceStatus.PAID, targetStatus);
        
        // Then only CANCELLED and PAID (idempotency) should be allowed
        boolean shouldBeAllowed = targetStatus == InvoiceStatus.CANCELLED
                || targetStatus == InvoiceStatus.PAID;
        
        assertEquals(shouldBeAllowed, isAllowed,
                String.format("PAID → %s should be %s", 
                    targetStatus, shouldBeAllowed ? "allowed" : "rejected"));
        
        // Specifically verify the three forbidden transitions
        if (targetStatus == InvoiceStatus.SENT) {
            assertFalse(isAllowed, "PAID → SENT should be rejected (Requirement 7.9)");
        } else if (targetStatus == InvoiceStatus.VIEWED) {
            assertFalse(isAllowed, "PAID → VIEWED should be rejected (Requirement 7.10)");
        } else if (targetStatus == InvoiceStatus.OVERDUE) {
            assertFalse(isAllowed, "PAID → OVERDUE should be rejected (Requirement 7.11)");
        }
    }
    
    /**
     * Property: CANCELLED is a terminal state
     * 
     * Verifies that CANCELLED status only allows transition to itself,
     * making it a true terminal state in the invoice lifecycle.
     */
    @Property(tries = 100)
    void cancelledIsTerminalState(
            @ForAll InvoiceStatus targetStatus) {
        
        // When checking transitions from CANCELLED
        boolean isAllowed = engine.isTransitionAllowed(InvoiceStatus.CANCELLED, targetStatus);
        
        // Then only CANCELLED (idempotency) should be allowed
        boolean shouldBeAllowed = targetStatus == InvoiceStatus.CANCELLED;
        
        assertEquals(shouldBeAllowed, isAllowed,
                String.format("CANCELLED → %s should be %s", 
                    targetStatus, shouldBeAllowed ? "allowed" : "rejected"));
    }
    
    /**
     * Property: Transition validation is consistent with isTransitionAllowed
     * 
     * Verifies that validateTransition throws an exception if and only if
     * isTransitionAllowed returns false.
     */
    @Property(tries = 100)
    void validateTransitionConsistentWithIsTransitionAllowed(
            @ForAll InvoiceStatus fromStatus,
            @ForAll InvoiceStatus toStatus) {
        
        // When checking if a transition is allowed
        boolean isAllowed = engine.isTransitionAllowed(fromStatus, toStatus);
        
        if (isAllowed) {
            // Then validateTransition should not throw an exception
            assertDoesNotThrow(() -> engine.validateTransition(fromStatus, toStatus),
                    String.format("validateTransition should not throw for allowed transition %s → %s", 
                        fromStatus, toStatus));
        } else {
            // Then validateTransition should throw an IllegalStateException
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> engine.validateTransition(fromStatus, toStatus),
                    String.format("validateTransition should throw for rejected transition %s → %s", 
                        fromStatus, toStatus));
            
            // Verify exception is not null (to satisfy warning)
            assertNotNull(exception, "Exception should not be null");
        }
    }
    
    // ==================== Generators ====================
    
    /**
     * Generator for all valid status transitions according to the state machine.
     * 
     * This generator produces all transitions that should be allowed:
     * - DRAFT → SENT, CANCELLED
     * - SENT → VIEWED, PAID, OVERDUE, CANCELLED
     * - VIEWED → PAID, OVERDUE, CANCELLED
     * - OVERDUE → PAID, CANCELLED
     * - PAID → CANCELLED
     * 
     * Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8
     */
    @Provide
    Arbitrary<StatusTransition> validTransitions() {
        return Arbitraries.of(
                // DRAFT transitions (Requirement 7.1, 7.8)
                new StatusTransition(InvoiceStatus.DRAFT, InvoiceStatus.SENT),
                new StatusTransition(InvoiceStatus.DRAFT, InvoiceStatus.CANCELLED),
                
                // SENT transitions (Requirements 7.2, 7.3, 7.5, 7.8)
                new StatusTransition(InvoiceStatus.SENT, InvoiceStatus.VIEWED),
                new StatusTransition(InvoiceStatus.SENT, InvoiceStatus.PAID),
                new StatusTransition(InvoiceStatus.SENT, InvoiceStatus.OVERDUE),
                new StatusTransition(InvoiceStatus.SENT, InvoiceStatus.CANCELLED),
                
                // VIEWED transitions (Requirements 7.4, 7.6, 7.8)
                new StatusTransition(InvoiceStatus.VIEWED, InvoiceStatus.PAID),
                new StatusTransition(InvoiceStatus.VIEWED, InvoiceStatus.OVERDUE),
                new StatusTransition(InvoiceStatus.VIEWED, InvoiceStatus.CANCELLED),
                
                // OVERDUE transitions (Requirements 7.7, 7.8)
                new StatusTransition(InvoiceStatus.OVERDUE, InvoiceStatus.PAID),
                new StatusTransition(InvoiceStatus.OVERDUE, InvoiceStatus.CANCELLED),
                
                // PAID transitions (Requirement 7.8)
                new StatusTransition(InvoiceStatus.PAID, InvoiceStatus.CANCELLED)
        );
    }
    
    /**
     * Generator for all invalid status transitions according to the state machine.
     * 
     * This generator produces transitions that should be rejected:
     * - DRAFT → VIEWED, PAID, OVERDUE (not allowed from DRAFT)
     * - SENT → DRAFT (cannot go back to DRAFT)
     * - VIEWED → DRAFT, SENT (cannot go back)
     * - PAID → SENT, VIEWED, OVERDUE, DRAFT (Requirements 7.9, 7.10, 7.11)
     * - OVERDUE → DRAFT, SENT, VIEWED (cannot go back)
     * - CANCELLED → any status except CANCELLED (terminal state)
     * 
     * Requirements: 7.9, 7.10, 7.11
     */
    @Provide
    Arbitrary<StatusTransition> invalidTransitions() {
        return Arbitraries.of(
                // DRAFT invalid transitions
                new StatusTransition(InvoiceStatus.DRAFT, InvoiceStatus.VIEWED),
                new StatusTransition(InvoiceStatus.DRAFT, InvoiceStatus.PAID),
                new StatusTransition(InvoiceStatus.DRAFT, InvoiceStatus.OVERDUE),
                
                // SENT invalid transitions
                new StatusTransition(InvoiceStatus.SENT, InvoiceStatus.DRAFT),
                
                // VIEWED invalid transitions
                new StatusTransition(InvoiceStatus.VIEWED, InvoiceStatus.DRAFT),
                new StatusTransition(InvoiceStatus.VIEWED, InvoiceStatus.SENT),
                
                // PAID invalid transitions (Requirements 7.9, 7.10, 7.11)
                new StatusTransition(InvoiceStatus.PAID, InvoiceStatus.DRAFT),
                new StatusTransition(InvoiceStatus.PAID, InvoiceStatus.SENT),    // Requirement 7.9
                new StatusTransition(InvoiceStatus.PAID, InvoiceStatus.VIEWED),  // Requirement 7.10
                new StatusTransition(InvoiceStatus.PAID, InvoiceStatus.OVERDUE), // Requirement 7.11
                
                // OVERDUE invalid transitions
                new StatusTransition(InvoiceStatus.OVERDUE, InvoiceStatus.DRAFT),
                new StatusTransition(InvoiceStatus.OVERDUE, InvoiceStatus.SENT),
                new StatusTransition(InvoiceStatus.OVERDUE, InvoiceStatus.VIEWED),
                
                // CANCELLED invalid transitions (terminal state)
                new StatusTransition(InvoiceStatus.CANCELLED, InvoiceStatus.DRAFT),
                new StatusTransition(InvoiceStatus.CANCELLED, InvoiceStatus.SENT),
                new StatusTransition(InvoiceStatus.CANCELLED, InvoiceStatus.VIEWED),
                new StatusTransition(InvoiceStatus.CANCELLED, InvoiceStatus.PAID),
                new StatusTransition(InvoiceStatus.CANCELLED, InvoiceStatus.OVERDUE)
        );
    }
    
    // ==================== Helper Classes ====================
    
    /**
     * Helper class to represent a status transition.
     * Used by property test generators to create transition test cases.
     */
    static class StatusTransition {
        final InvoiceStatus from;
        final InvoiceStatus to;
        
        StatusTransition(InvoiceStatus from, InvoiceStatus to) {
            this.from = from;
            this.to = to;
        }
        
        @Override
        public String toString() {
            return from + " → " + to;
        }
    }
}
