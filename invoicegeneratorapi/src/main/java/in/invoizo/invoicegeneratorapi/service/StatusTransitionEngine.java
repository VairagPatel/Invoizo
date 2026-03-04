package in.invoizo.invoicegeneratorapi.service;

import in.invoizo.invoicegeneratorapi.entity.Invoice.InvoiceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

/**
 * Core component responsible for executing and validating invoice status transitions.
 * Enforces state machine rules and ensures data consistency across all status changes.
 * 
 * This engine implements the state machine defined in the design document:
 * - DRAFT → SENT, CANCELLED
 * - SENT → VIEWED, PAID, OVERDUE, CANCELLED
 * - VIEWED → PAID, OVERDUE, CANCELLED
 * - OVERDUE → PAID, CANCELLED
 * - PAID → CANCELLED (only)
 * - CANCELLED → (terminal state)
 * 
 * Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8, 7.9, 7.10, 7.11, 10.3
 */
@Service
public class StatusTransitionEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(StatusTransitionEngine.class);
    
    /**
     * Transition Rules Matrix
     * Defines all allowed status transitions in the invoice lifecycle.
     * 
     * From/To     | DRAFT | SENT | VIEWED | PAID | OVERDUE | CANCELLED |
     * ------------|-------|------|--------|------|---------|-----------|
     * DRAFT       | ✓     | ✓    | ✗      | ✗    | ✗       | ✓         |
     * SENT        | ✗     | ✓    | ✓      | ✓    | ✓       | ✓         |
     * VIEWED      | ✗     | ✗    | ✓      | ✓    | ✓       | ✓         |
     * PAID        | ✗     | ✗    | ✗      | ✓    | ✗       | ✓         |
     * OVERDUE     | ✗     | ✗    | ✗      | ✓    | ✓       | ✓         |
     * CANCELLED   | ✗     | ✗    | ✗      | ✗    | ✗       | ✓         |
     * 
     * Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8, 7.9, 7.10, 7.11
     */
    private static final Map<InvoiceStatus, Set<InvoiceStatus>> ALLOWED_TRANSITIONS = Map.of(
        InvoiceStatus.DRAFT, Set.of(
            InvoiceStatus.DRAFT,      // Allow same-state (for updates)
            InvoiceStatus.SENT,       // Requirement 7.1: DRAFT → SENT
            InvoiceStatus.CANCELLED   // Requirement 7.8: Any status → CANCELLED
        ),
        InvoiceStatus.SENT, Set.of(
            InvoiceStatus.SENT,       // Allow same-state (for idempotency)
            InvoiceStatus.VIEWED,     // Requirement 7.2: SENT → VIEWED
            InvoiceStatus.PAID,       // Requirement 7.5: SENT → PAID
            InvoiceStatus.OVERDUE,    // Requirement 7.3: SENT → OVERDUE
            InvoiceStatus.CANCELLED   // Requirement 7.8: Any status → CANCELLED
        ),
        InvoiceStatus.VIEWED, Set.of(
            InvoiceStatus.VIEWED,     // Allow same-state (for idempotency)
            InvoiceStatus.PAID,       // Requirement 7.6: VIEWED → PAID
            InvoiceStatus.OVERDUE,    // Requirement 7.4: VIEWED → OVERDUE
            InvoiceStatus.CANCELLED   // Requirement 7.8: Any status → CANCELLED
        ),
        InvoiceStatus.OVERDUE, Set.of(
            InvoiceStatus.OVERDUE,    // Allow same-state (for idempotency)
            InvoiceStatus.PAID,       // Requirement 7.7: OVERDUE → PAID
            InvoiceStatus.CANCELLED   // Requirement 7.8: Any status → CANCELLED
        ),
        InvoiceStatus.PAID, Set.of(
            InvoiceStatus.PAID,       // Allow same-state (for idempotency)
            InvoiceStatus.CANCELLED   // Requirement 7.8: Any status → CANCELLED (including PAID)
            // Requirement 7.9: PAID → SENT is NOT allowed (not in set)
            // Requirement 7.10: PAID → VIEWED is NOT allowed (not in set)
            // Requirement 7.11: PAID → OVERDUE is NOT allowed (not in set)
        ),
        InvoiceStatus.CANCELLED, Set.of(
            InvoiceStatus.CANCELLED   // Terminal state - only allow same-state
        )
    );
    
    /**
     * Validates if a status transition is allowed according to the state machine rules.
     * 
     * This method checks the transition rules matrix to determine if moving from
     * currentStatus to targetStatus is permitted. Same-state transitions are allowed
     * for idempotency (e.g., when a webhook is received multiple times).
     * 
     * @param currentStatus The current status of the invoice
     * @param targetStatus The desired target status
     * @return true if the transition is allowed, false otherwise
     * 
     * Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8, 7.9, 7.10, 7.11, 10.3
     */
    public boolean isTransitionAllowed(InvoiceStatus currentStatus, InvoiceStatus targetStatus) {
        // Handle null inputs
        if (currentStatus == null || targetStatus == null) {
            logger.warn("Null status provided for transition validation: current={}, target={}", 
                currentStatus, targetStatus);
            return false;
        }
        
        // Get allowed transitions for the current status
        Set<InvoiceStatus> allowedTargets = ALLOWED_TRANSITIONS.get(currentStatus);
        
        // If no transitions defined for current status, reject
        if (allowedTargets == null) {
            logger.warn("No transitions defined for status: {}", currentStatus);
            return false;
        }
        
        // Check if target status is in the allowed set
        boolean isAllowed = allowedTargets.contains(targetStatus);
        
        if (!isAllowed) {
            logger.debug("Transition not allowed: {} → {}", currentStatus, targetStatus);
        }
        
        return isAllowed;
    }
    
    /**
     * Validates a transition and throws an exception if not allowed.
     * This is a convenience method for code that wants to fail fast on invalid transitions.
     * 
     * @param currentStatus The current status of the invoice
     * @param targetStatus The desired target status
     * @throws IllegalStateException if the transition is not allowed
     * 
     * Requirements: 10.3
     */
    public void validateTransition(InvoiceStatus currentStatus, InvoiceStatus targetStatus) {
        if (!isTransitionAllowed(currentStatus, targetStatus)) {
            String message = String.format(
                "Invalid status transition: Cannot transition from %s to %s. " +
                "Allowed transitions from %s are: %s",
                currentStatus, 
                targetStatus,
                currentStatus,
                ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of())
            );
            logger.error(message);
            throw new IllegalStateException(message);
        }
    }
    
    /**
     * Gets the set of allowed target statuses for a given current status.
     * Useful for UI/API to show available actions.
     * 
     * @param currentStatus The current status
     * @return Set of allowed target statuses (excluding same-state transitions)
     */
    public Set<InvoiceStatus> getAllowedTransitions(InvoiceStatus currentStatus) {
        if (currentStatus == null) {
            return Set.of();
        }
        
        Set<InvoiceStatus> allowed = ALLOWED_TRANSITIONS.get(currentStatus);
        if (allowed == null) {
            return Set.of();
        }
        
        // Return a copy to prevent external modification
        return Set.copyOf(allowed);
    }
    
    /**
     * Checks if a status is a terminal state (no outgoing transitions except to itself).
     * 
     * @param status The status to check
     * @return true if the status is terminal (PAID or CANCELLED)
     */
    public boolean isTerminalState(InvoiceStatus status) {
        if (status == null) {
            return false;
        }
        
        Set<InvoiceStatus> allowed = ALLOWED_TRANSITIONS.get(status);
        if (allowed == null) {
            return true;
        }
        
        // A terminal state only allows transition to itself or CANCELLED
        // PAID can transition to CANCELLED, so it's not fully terminal
        // CANCELLED can only transition to itself, so it's fully terminal
        return status == InvoiceStatus.CANCELLED;
    }
}
