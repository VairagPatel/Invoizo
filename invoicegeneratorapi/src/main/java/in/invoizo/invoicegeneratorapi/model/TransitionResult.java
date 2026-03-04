package in.invoizo.invoicegeneratorapi.model;

import in.invoizo.invoicegeneratorapi.entity.Invoice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result object returned by StatusTransitionEngine after attempting a status transition.
 * Contains information about whether the transition succeeded, the updated invoice,
 * and any error messages that occurred during the transition.
 * 
 * Requirements: 7.1-7.11
 */
public class TransitionResult {
    private final boolean success;
    private final Invoice updatedInvoice;
    private final List<String> errorMessages;

    private TransitionResult(boolean success, Invoice updatedInvoice, List<String> errorMessages) {
        this.success = success;
        this.updatedInvoice = updatedInvoice;
        this.errorMessages = errorMessages != null ? 
            Collections.unmodifiableList(new ArrayList<>(errorMessages)) : 
            Collections.emptyList();
    }

    /**
     * Creates a successful transition result.
     * 
     * @param updatedInvoice The invoice after the successful transition
     * @return A successful TransitionResult
     */
    public static TransitionResult success(Invoice updatedInvoice) {
        if (updatedInvoice == null) {
            throw new IllegalArgumentException("Updated invoice cannot be null for a successful transition");
        }
        return new TransitionResult(true, updatedInvoice, Collections.emptyList());
    }

    /**
     * Creates a failed transition result with a single error message.
     * 
     * @param errorMessage The error message describing why the transition failed
     * @return A failed TransitionResult
     */
    public static TransitionResult failure(String errorMessage) {
        if (errorMessage == null || errorMessage.trim().isEmpty()) {
            throw new IllegalArgumentException("Error message cannot be null or empty for a failed transition");
        }
        return new TransitionResult(false, null, Collections.singletonList(errorMessage));
    }

    /**
     * Creates a failed transition result with multiple error messages.
     * 
     * @param errorMessages The list of error messages describing why the transition failed
     * @return A failed TransitionResult
     */
    public static TransitionResult failure(List<String> errorMessages) {
        if (errorMessages == null || errorMessages.isEmpty()) {
            throw new IllegalArgumentException("Error messages cannot be null or empty for a failed transition");
        }
        return new TransitionResult(false, null, errorMessages);
    }

    /**
     * Creates a failed transition result with the current invoice state and error message.
     * Useful when you want to return the invoice in its current state along with the error.
     * 
     * @param currentInvoice The invoice in its current state (before failed transition)
     * @param errorMessage The error message describing why the transition failed
     * @return A failed TransitionResult
     */
    public static TransitionResult failureWithInvoice(Invoice currentInvoice, String errorMessage) {
        if (errorMessage == null || errorMessage.trim().isEmpty()) {
            throw new IllegalArgumentException("Error message cannot be null or empty for a failed transition");
        }
        return new TransitionResult(false, currentInvoice, Collections.singletonList(errorMessage));
    }

    /**
     * Returns whether the transition was successful.
     * 
     * @return true if the transition succeeded, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns whether the transition failed.
     * 
     * @return true if the transition failed, false otherwise
     */
    public boolean isFailure() {
        return !success;
    }

    /**
     * Returns the updated invoice after a successful transition,
     * or the current invoice state for a failed transition (if provided).
     * 
     * @return The invoice, or null if the transition failed and no invoice was provided
     */
    public Invoice getUpdatedInvoice() {
        return updatedInvoice;
    }

    /**
     * Returns the list of error messages if the transition failed.
     * Returns an empty list if the transition succeeded.
     * 
     * @return An unmodifiable list of error messages
     */
    public List<String> getErrorMessages() {
        return errorMessages;
    }

    /**
     * Returns a single error message string by joining all error messages.
     * Useful for logging or displaying a single error message.
     * 
     * @return A single string containing all error messages, or empty string if no errors
     */
    public String getErrorMessage() {
        return String.join("; ", errorMessages);
    }

    /**
     * Returns whether there are any error messages.
     * 
     * @return true if there are error messages, false otherwise
     */
    public boolean hasErrors() {
        return !errorMessages.isEmpty();
    }

    @Override
    public String toString() {
        if (success) {
            return "TransitionResult{success=true, invoice=" + 
                (updatedInvoice != null ? updatedInvoice.getId() : "null") + "}";
        } else {
            return "TransitionResult{success=false, errors=" + errorMessages + 
                (updatedInvoice != null ? ", invoice=" + updatedInvoice.getId() : "") + "}";
        }
    }
}
