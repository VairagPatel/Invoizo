package in.invoizo.invoicegeneratorapi.service;

import in.invoizo.invoicegeneratorapi.entity.Invoice.InvoiceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StatusTransitionEngine.
 * Tests the state machine rules and transition validation logic.
 * 
 * Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8, 7.9, 7.10, 7.11, 10.3
 */
@DisplayName("StatusTransitionEngine Tests")
class StatusTransitionEngineTest {
    
    private StatusTransitionEngine engine;
    
    @BeforeEach
    void setUp() {
        engine = new StatusTransitionEngine();
    }
    
    @Nested
    @DisplayName("Allowed Transitions - Requirements 7.1-7.8")
    class AllowedTransitionsTests {
        
        @Test
        @DisplayName("Requirement 7.1: DRAFT → SENT is allowed")
        void testDraftToSent() {
            assertTrue(engine.isTransitionAllowed(InvoiceStatus.DRAFT, InvoiceStatus.SENT),
                "Should allow transition from DRAFT to SENT");
        }
        
        @Test
        @DisplayName("Requirement 7.2: SENT → VIEWED is allowed")
        void testSentToViewed() {
            assertTrue(engine.isTransitionAllowed(InvoiceStatus.SENT, InvoiceStatus.VIEWED),
                "Should allow transition from SENT to VIEWED");
        }
        
        @Test
        @DisplayName("Requirement 7.3: SENT → OVERDUE is allowed")
        void testSentToOverdue() {
            assertTrue(engine.isTransitionAllowed(InvoiceStatus.SENT, InvoiceStatus.OVERDUE),
                "Should allow transition from SENT to OVERDUE");
        }
        
        @Test
        @DisplayName("Requirement 7.4: VIEWED → OVERDUE is allowed")
        void testViewedToOverdue() {
            assertTrue(engine.isTransitionAllowed(InvoiceStatus.VIEWED, InvoiceStatus.OVERDUE),
                "Should allow transition from VIEWED to OVERDUE");
        }
        
        @Test
        @DisplayName("Requirement 7.5: SENT → PAID is allowed")
        void testSentToPaid() {
            assertTrue(engine.isTransitionAllowed(InvoiceStatus.SENT, InvoiceStatus.PAID),
                "Should allow transition from SENT to PAID");
        }
        
        @Test
        @DisplayName("Requirement 7.6: VIEWED → PAID is allowed")
        void testViewedToPaid() {
            assertTrue(engine.isTransitionAllowed(InvoiceStatus.VIEWED, InvoiceStatus.PAID),
                "Should allow transition from VIEWED to PAID");
        }
        
        @Test
        @DisplayName("Requirement 7.7: OVERDUE → PAID is allowed")
        void testOverdueToPaid() {
            assertTrue(engine.isTransitionAllowed(InvoiceStatus.OVERDUE, InvoiceStatus.PAID),
                "Should allow transition from OVERDUE to PAID");
        }
        
        @Test
        @DisplayName("Requirement 7.8: Any status → CANCELLED is allowed")
        void testAnyCancelled() {
            // Test all statuses can transition to CANCELLED
            assertTrue(engine.isTransitionAllowed(InvoiceStatus.DRAFT, InvoiceStatus.CANCELLED),
                "Should allow transition from DRAFT to CANCELLED");
            assertTrue(engine.isTransitionAllowed(InvoiceStatus.SENT, InvoiceStatus.CANCELLED),
                "Should allow transition from SENT to CANCELLED");
            assertTrue(engine.isTransitionAllowed(InvoiceStatus.VIEWED, InvoiceStatus.CANCELLED),
                "Should allow transition from VIEWED to CANCELLED");
            assertTrue(engine.isTransitionAllowed(InvoiceStatus.OVERDUE, InvoiceStatus.CANCELLED),
                "Should allow transition from OVERDUE to CANCELLED");
            assertTrue(engine.isTransitionAllowed(InvoiceStatus.PAID, InvoiceStatus.CANCELLED),
                "Should allow transition from PAID to CANCELLED");
        }
    }
    
    @Nested
    @DisplayName("Rejected Transitions - Requirements 7.9-7.11")
    class RejectedTransitionsTests {
        
        @Test
        @DisplayName("Requirement 7.9: PAID → SENT is rejected")
        void testPaidToSent() {
            assertFalse(engine.isTransitionAllowed(InvoiceStatus.PAID, InvoiceStatus.SENT),
                "Should reject transition from PAID to SENT");
        }
        
        @Test
        @DisplayName("Requirement 7.10: PAID → VIEWED is rejected")
        void testPaidToViewed() {
            assertFalse(engine.isTransitionAllowed(InvoiceStatus.PAID, InvoiceStatus.VIEWED),
                "Should reject transition from PAID to VIEWED");
        }
        
        @Test
        @DisplayName("Requirement 7.11: PAID → OVERDUE is rejected")
        void testPaidToOverdue() {
            assertFalse(engine.isTransitionAllowed(InvoiceStatus.PAID, InvoiceStatus.OVERDUE),
                "Should reject transition from PAID to OVERDUE");
        }
        
        @Test
        @DisplayName("DRAFT → VIEWED is rejected (not in allowed transitions)")
        void testDraftToViewed() {
            assertFalse(engine.isTransitionAllowed(InvoiceStatus.DRAFT, InvoiceStatus.VIEWED),
                "Should reject transition from DRAFT to VIEWED");
        }
        
        @Test
        @DisplayName("DRAFT → PAID is rejected (not in allowed transitions)")
        void testDraftToPaid() {
            assertFalse(engine.isTransitionAllowed(InvoiceStatus.DRAFT, InvoiceStatus.PAID),
                "Should reject transition from DRAFT to PAID");
        }
        
        @Test
        @DisplayName("DRAFT → OVERDUE is rejected (not in allowed transitions)")
        void testDraftToOverdue() {
            assertFalse(engine.isTransitionAllowed(InvoiceStatus.DRAFT, InvoiceStatus.OVERDUE),
                "Should reject transition from DRAFT to OVERDUE");
        }
        
        @Test
        @DisplayName("VIEWED → SENT is rejected (backward transition)")
        void testViewedToSent() {
            assertFalse(engine.isTransitionAllowed(InvoiceStatus.VIEWED, InvoiceStatus.SENT),
                "Should reject backward transition from VIEWED to SENT");
        }
        
        @Test
        @DisplayName("VIEWED → DRAFT is rejected (backward transition)")
        void testViewedToDraft() {
            assertFalse(engine.isTransitionAllowed(InvoiceStatus.VIEWED, InvoiceStatus.DRAFT),
                "Should reject backward transition from VIEWED to DRAFT");
        }
        
        @Test
        @DisplayName("OVERDUE → SENT is rejected (backward transition)")
        void testOverdueToSent() {
            assertFalse(engine.isTransitionAllowed(InvoiceStatus.OVERDUE, InvoiceStatus.SENT),
                "Should reject backward transition from OVERDUE to SENT");
        }
        
        @Test
        @DisplayName("OVERDUE → VIEWED is rejected (backward transition)")
        void testOverdueToViewed() {
            assertFalse(engine.isTransitionAllowed(InvoiceStatus.OVERDUE, InvoiceStatus.VIEWED),
                "Should reject backward transition from OVERDUE to VIEWED");
        }
        
        @Test
        @DisplayName("OVERDUE → DRAFT is rejected (backward transition)")
        void testOverdueToDraft() {
            assertFalse(engine.isTransitionAllowed(InvoiceStatus.OVERDUE, InvoiceStatus.DRAFT),
                "Should reject backward transition from OVERDUE to DRAFT");
        }
        
        @Test
        @DisplayName("CANCELLED → any status is rejected (terminal state)")
        void testCancelledToAny() {
            assertFalse(engine.isTransitionAllowed(InvoiceStatus.CANCELLED, InvoiceStatus.DRAFT),
                "Should reject transition from CANCELLED to DRAFT");
            assertFalse(engine.isTransitionAllowed(InvoiceStatus.CANCELLED, InvoiceStatus.SENT),
                "Should reject transition from CANCELLED to SENT");
            assertFalse(engine.isTransitionAllowed(InvoiceStatus.CANCELLED, InvoiceStatus.VIEWED),
                "Should reject transition from CANCELLED to VIEWED");
            assertFalse(engine.isTransitionAllowed(InvoiceStatus.CANCELLED, InvoiceStatus.PAID),
                "Should reject transition from CANCELLED to PAID");
            assertFalse(engine.isTransitionAllowed(InvoiceStatus.CANCELLED, InvoiceStatus.OVERDUE),
                "Should reject transition from CANCELLED to OVERDUE");
        }
    }
    
    @Nested
    @DisplayName("Idempotency - Same State Transitions")
    class IdempotencyTests {
        
        @Test
        @DisplayName("Same-state transitions are allowed for idempotency")
        void testSameStateTransitions() {
            // All statuses should allow transition to themselves for idempotency
            assertTrue(engine.isTransitionAllowed(InvoiceStatus.DRAFT, InvoiceStatus.DRAFT),
                "Should allow DRAFT → DRAFT for idempotency");
            assertTrue(engine.isTransitionAllowed(InvoiceStatus.SENT, InvoiceStatus.SENT),
                "Should allow SENT → SENT for idempotency");
            assertTrue(engine.isTransitionAllowed(InvoiceStatus.VIEWED, InvoiceStatus.VIEWED),
                "Should allow VIEWED → VIEWED for idempotency");
            assertTrue(engine.isTransitionAllowed(InvoiceStatus.PAID, InvoiceStatus.PAID),
                "Should allow PAID → PAID for idempotency");
            assertTrue(engine.isTransitionAllowed(InvoiceStatus.OVERDUE, InvoiceStatus.OVERDUE),
                "Should allow OVERDUE → OVERDUE for idempotency");
            assertTrue(engine.isTransitionAllowed(InvoiceStatus.CANCELLED, InvoiceStatus.CANCELLED),
                "Should allow CANCELLED → CANCELLED for idempotency");
        }
    }
    
    @Nested
    @DisplayName("Null Handling")
    class NullHandlingTests {
        
        @Test
        @DisplayName("Null current status is rejected")
        void testNullCurrentStatus() {
            assertFalse(engine.isTransitionAllowed(null, InvoiceStatus.SENT),
                "Should reject null current status");
        }
        
        @Test
        @DisplayName("Null target status is rejected")
        void testNullTargetStatus() {
            assertFalse(engine.isTransitionAllowed(InvoiceStatus.DRAFT, null),
                "Should reject null target status");
        }
        
        @Test
        @DisplayName("Both null statuses are rejected")
        void testBothNull() {
            assertFalse(engine.isTransitionAllowed(null, null),
                "Should reject both null statuses");
        }
    }
    
    @Nested
    @DisplayName("Validation Method - Requirement 10.3")
    class ValidationMethodTests {
        
        @Test
        @DisplayName("validateTransition succeeds for allowed transition")
        void testValidateAllowedTransition() {
            assertDoesNotThrow(() -> 
                engine.validateTransition(InvoiceStatus.DRAFT, InvoiceStatus.SENT),
                "Should not throw exception for allowed transition");
        }
        
        @Test
        @DisplayName("validateTransition throws exception for rejected transition")
        void testValidateRejectedTransition() {
            IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                engine.validateTransition(InvoiceStatus.PAID, InvoiceStatus.SENT),
                "Should throw exception for rejected transition");
            
            assertTrue(exception.getMessage().contains("Invalid status transition"),
                "Exception message should mention invalid transition");
            assertTrue(exception.getMessage().contains("PAID"),
                "Exception message should mention current status");
            assertTrue(exception.getMessage().contains("SENT"),
                "Exception message should mention target status");
        }
        
        @Test
        @DisplayName("validateTransition throws exception for null status")
        void testValidateNullStatus() {
            assertThrows(IllegalStateException.class, () ->
                engine.validateTransition(null, InvoiceStatus.SENT),
                "Should throw exception for null current status");
            
            assertThrows(IllegalStateException.class, () ->
                engine.validateTransition(InvoiceStatus.DRAFT, null),
                "Should throw exception for null target status");
        }
    }
    
    @Nested
    @DisplayName("Helper Methods")
    class HelperMethodsTests {
        
        @Test
        @DisplayName("getAllowedTransitions returns correct set for DRAFT")
        void testGetAllowedTransitionsDraft() {
            Set<InvoiceStatus> allowed = engine.getAllowedTransitions(InvoiceStatus.DRAFT);
            
            assertTrue(allowed.contains(InvoiceStatus.DRAFT), "Should include DRAFT");
            assertTrue(allowed.contains(InvoiceStatus.SENT), "Should include SENT");
            assertTrue(allowed.contains(InvoiceStatus.CANCELLED), "Should include CANCELLED");
            assertEquals(3, allowed.size(), "Should have exactly 3 allowed transitions");
        }
        
        @Test
        @DisplayName("getAllowedTransitions returns correct set for SENT")
        void testGetAllowedTransitionsSent() {
            Set<InvoiceStatus> allowed = engine.getAllowedTransitions(InvoiceStatus.SENT);
            
            assertTrue(allowed.contains(InvoiceStatus.SENT), "Should include SENT");
            assertTrue(allowed.contains(InvoiceStatus.VIEWED), "Should include VIEWED");
            assertTrue(allowed.contains(InvoiceStatus.PAID), "Should include PAID");
            assertTrue(allowed.contains(InvoiceStatus.OVERDUE), "Should include OVERDUE");
            assertTrue(allowed.contains(InvoiceStatus.CANCELLED), "Should include CANCELLED");
            assertEquals(5, allowed.size(), "Should have exactly 5 allowed transitions");
        }
        
        @Test
        @DisplayName("getAllowedTransitions returns correct set for PAID")
        void testGetAllowedTransitionsPaid() {
            Set<InvoiceStatus> allowed = engine.getAllowedTransitions(InvoiceStatus.PAID);
            
            assertTrue(allowed.contains(InvoiceStatus.PAID), "Should include PAID");
            assertTrue(allowed.contains(InvoiceStatus.CANCELLED), "Should include CANCELLED");
            assertEquals(2, allowed.size(), "Should have exactly 2 allowed transitions");
        }
        
        @Test
        @DisplayName("getAllowedTransitions returns empty set for null")
        void testGetAllowedTransitionsNull() {
            Set<InvoiceStatus> allowed = engine.getAllowedTransitions(null);
            assertTrue(allowed.isEmpty(), "Should return empty set for null status");
        }
        
        @Test
        @DisplayName("isTerminalState returns true for CANCELLED")
        void testIsTerminalStateCancelled() {
            assertTrue(engine.isTerminalState(InvoiceStatus.CANCELLED),
                "CANCELLED should be a terminal state");
        }
        
        @Test
        @DisplayName("isTerminalState returns false for non-terminal states")
        void testIsTerminalStateNonTerminal() {
            assertFalse(engine.isTerminalState(InvoiceStatus.DRAFT),
                "DRAFT should not be a terminal state");
            assertFalse(engine.isTerminalState(InvoiceStatus.SENT),
                "SENT should not be a terminal state");
            assertFalse(engine.isTerminalState(InvoiceStatus.VIEWED),
                "VIEWED should not be a terminal state");
            assertFalse(engine.isTerminalState(InvoiceStatus.OVERDUE),
                "OVERDUE should not be a terminal state");
            assertFalse(engine.isTerminalState(InvoiceStatus.PAID),
                "PAID should not be a terminal state (can transition to CANCELLED)");
        }
        
        @Test
        @DisplayName("isTerminalState returns false for null")
        void testIsTerminalStateNull() {
            assertFalse(engine.isTerminalState(null),
                "Should return false for null status");
        }
    }
    
    @Nested
    @DisplayName("Comprehensive State Machine Coverage")
    class ComprehensiveTests {
        
        @Test
        @DisplayName("All valid transitions from design document are allowed")
        void testAllValidTransitions() {
            // DRAFT transitions
            assertTrue(engine.isTransitionAllowed(InvoiceStatus.DRAFT, InvoiceStatus.SENT));
            assertTrue(engine.isTransitionAllowed(InvoiceStatus.DRAFT, InvoiceStatus.CANCELLED));
            
            // SENT transitions
            assertTrue(engine.isTransitionAllowed(InvoiceStatus.SENT, InvoiceStatus.VIEWED));
            assertTrue(engine.isTransitionAllowed(InvoiceStatus.SENT, InvoiceStatus.PAID));
            assertTrue(engine.isTransitionAllowed(InvoiceStatus.SENT, InvoiceStatus.OVERDUE));
            assertTrue(engine.isTransitionAllowed(InvoiceStatus.SENT, InvoiceStatus.CANCELLED));
            
            // VIEWED transitions
            assertTrue(engine.isTransitionAllowed(InvoiceStatus.VIEWED, InvoiceStatus.PAID));
            assertTrue(engine.isTransitionAllowed(InvoiceStatus.VIEWED, InvoiceStatus.OVERDUE));
            assertTrue(engine.isTransitionAllowed(InvoiceStatus.VIEWED, InvoiceStatus.CANCELLED));
            
            // OVERDUE transitions
            assertTrue(engine.isTransitionAllowed(InvoiceStatus.OVERDUE, InvoiceStatus.PAID));
            assertTrue(engine.isTransitionAllowed(InvoiceStatus.OVERDUE, InvoiceStatus.CANCELLED));
            
            // PAID transitions
            assertTrue(engine.isTransitionAllowed(InvoiceStatus.PAID, InvoiceStatus.CANCELLED));
        }
        
        @Test
        @DisplayName("All invalid backward transitions are rejected")
        void testAllInvalidBackwardTransitions() {
            // Cannot go back to DRAFT from any other state
            assertFalse(engine.isTransitionAllowed(InvoiceStatus.SENT, InvoiceStatus.DRAFT));
            assertFalse(engine.isTransitionAllowed(InvoiceStatus.VIEWED, InvoiceStatus.DRAFT));
            assertFalse(engine.isTransitionAllowed(InvoiceStatus.PAID, InvoiceStatus.DRAFT));
            assertFalse(engine.isTransitionAllowed(InvoiceStatus.OVERDUE, InvoiceStatus.DRAFT));
            assertFalse(engine.isTransitionAllowed(InvoiceStatus.CANCELLED, InvoiceStatus.DRAFT));
            
            // Cannot go back to SENT from later states (except SENT itself)
            assertFalse(engine.isTransitionAllowed(InvoiceStatus.VIEWED, InvoiceStatus.SENT));
            assertFalse(engine.isTransitionAllowed(InvoiceStatus.PAID, InvoiceStatus.SENT));
            assertFalse(engine.isTransitionAllowed(InvoiceStatus.OVERDUE, InvoiceStatus.SENT));
            assertFalse(engine.isTransitionAllowed(InvoiceStatus.CANCELLED, InvoiceStatus.SENT));
        }
    }
}
