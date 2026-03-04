package in.invoizo.invoicegeneratorapi.model;

import in.invoizo.invoicegeneratorapi.entity.Invoice;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for TransitionResult class.
 * Tests the result object returned by StatusTransitionEngine.
 * 
 * Validates Requirements: 7.1-7.11
 */
class TransitionResultTest {

    @Test
    void success_WithValidInvoice_CreatesSuccessfulResult() {
        // Arrange
        Invoice invoice = createTestInvoice("INV-001");

        // Act
        TransitionResult result = TransitionResult.success(invoice);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isFailure()).isFalse();
        assertThat(result.getUpdatedInvoice()).isEqualTo(invoice);
        assertThat(result.getErrorMessages()).isEmpty();
        assertThat(result.hasErrors()).isFalse();
        assertThat(result.getErrorMessage()).isEmpty();
    }

    @Test
    void success_WithNullInvoice_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> TransitionResult.success(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Updated invoice cannot be null");
    }

    @Test
    void failure_WithSingleErrorMessage_CreatesFailedResult() {
        // Arrange
        String errorMessage = "Invalid status transition from PAID to SENT";

        // Act
        TransitionResult result = TransitionResult.failure(errorMessage);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getUpdatedInvoice()).isNull();
        assertThat(result.getErrorMessages()).containsExactly(errorMessage);
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrorMessage()).isEqualTo(errorMessage);
    }

    @Test
    void failure_WithNullErrorMessage_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> TransitionResult.failure((String) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Error message cannot be null or empty");
    }

    @Test
    void failure_WithEmptyErrorMessage_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> TransitionResult.failure(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Error message cannot be null or empty");
    }

    @Test
    void failure_WithWhitespaceErrorMessage_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> TransitionResult.failure("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Error message cannot be null or empty");
    }

    @Test
    void failure_WithMultipleErrorMessages_CreatesFailedResult() {
        // Arrange
        List<String> errorMessages = Arrays.asList(
            "Invoice not found",
            "Invalid status transition",
            "Missing required field: payment_link_id"
        );

        // Act
        TransitionResult result = TransitionResult.failure(errorMessages);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getUpdatedInvoice()).isNull();
        assertThat(result.getErrorMessages()).containsExactlyElementsOf(errorMessages);
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrorMessage())
            .isEqualTo("Invoice not found; Invalid status transition; Missing required field: payment_link_id");
    }

    @Test
    void failure_WithNullErrorMessageList_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> TransitionResult.failure((List<String>) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Error messages cannot be null or empty");
    }

    @Test
    void failure_WithEmptyErrorMessageList_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> TransitionResult.failure(Collections.emptyList()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Error messages cannot be null or empty");
    }

    @Test
    void failureWithInvoice_WithValidInvoiceAndError_CreatesFailedResultWithInvoice() {
        // Arrange
        Invoice invoice = createTestInvoice("INV-002");
        String errorMessage = "Cannot transition from PAID to OVERDUE";

        // Act
        TransitionResult result = TransitionResult.failureWithInvoice(invoice, errorMessage);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getUpdatedInvoice()).isEqualTo(invoice);
        assertThat(result.getErrorMessages()).containsExactly(errorMessage);
        assertThat(result.hasErrors()).isTrue();
    }

    @Test
    void failureWithInvoice_WithNullInvoice_CreatesFailedResultWithoutInvoice() {
        // Arrange
        String errorMessage = "Invoice not found";

        // Act
        TransitionResult result = TransitionResult.failureWithInvoice(null, errorMessage);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getUpdatedInvoice()).isNull();
        assertThat(result.getErrorMessages()).containsExactly(errorMessage);
    }

    @Test
    void failureWithInvoice_WithNullErrorMessage_ThrowsException() {
        // Arrange
        Invoice invoice = createTestInvoice("INV-003");

        // Act & Assert
        assertThatThrownBy(() -> TransitionResult.failureWithInvoice(invoice, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Error message cannot be null or empty");
    }

    @Test
    void getErrorMessages_ReturnsUnmodifiableList() {
        // Arrange
        List<String> errorMessages = Arrays.asList("Error 1", "Error 2");
        TransitionResult result = TransitionResult.failure(errorMessages);

        // Act
        List<String> returnedErrors = result.getErrorMessages();

        // Assert
        assertThatThrownBy(() -> returnedErrors.add("Error 3"))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void getErrorMessage_WithNoErrors_ReturnsEmptyString() {
        // Arrange
        Invoice invoice = createTestInvoice("INV-004");
        TransitionResult result = TransitionResult.success(invoice);

        // Act
        String errorMessage = result.getErrorMessage();

        // Assert
        assertThat(errorMessage).isEmpty();
    }

    @Test
    void getErrorMessage_WithSingleError_ReturnsSingleMessage() {
        // Arrange
        TransitionResult result = TransitionResult.failure("Single error");

        // Act
        String errorMessage = result.getErrorMessage();

        // Assert
        assertThat(errorMessage).isEqualTo("Single error");
    }

    @Test
    void getErrorMessage_WithMultipleErrors_ReturnsJoinedMessages() {
        // Arrange
        List<String> errors = Arrays.asList("Error 1", "Error 2", "Error 3");
        TransitionResult result = TransitionResult.failure(errors);

        // Act
        String errorMessage = result.getErrorMessage();

        // Assert
        assertThat(errorMessage).isEqualTo("Error 1; Error 2; Error 3");
    }

    @Test
    void toString_ForSuccessfulResult_ContainsSuccessAndInvoiceId() {
        // Arrange
        Invoice invoice = createTestInvoice("INV-005");
        TransitionResult result = TransitionResult.success(invoice);

        // Act
        String toString = result.toString();

        // Assert
        assertThat(toString)
            .contains("success=true")
            .contains("INV-005");
    }

    @Test
    void toString_ForFailedResult_ContainsFailureAndErrors() {
        // Arrange
        TransitionResult result = TransitionResult.failure("Test error");

        // Act
        String toString = result.toString();

        // Assert
        assertThat(toString)
            .contains("success=false")
            .contains("Test error");
    }

    @Test
    void toString_ForFailedResultWithInvoice_ContainsFailureErrorsAndInvoiceId() {
        // Arrange
        Invoice invoice = createTestInvoice("INV-006");
        TransitionResult result = TransitionResult.failureWithInvoice(invoice, "Test error");

        // Act
        String toString = result.toString();

        // Assert
        assertThat(toString)
            .contains("success=false")
            .contains("Test error")
            .contains("INV-006");
    }

    @Test
    void hasErrors_WithSuccessfulResult_ReturnsFalse() {
        // Arrange
        Invoice invoice = createTestInvoice("INV-007");
        TransitionResult result = TransitionResult.success(invoice);

        // Act & Assert
        assertThat(result.hasErrors()).isFalse();
    }

    @Test
    void hasErrors_WithFailedResult_ReturnsTrue() {
        // Arrange
        TransitionResult result = TransitionResult.failure("Error occurred");

        // Act & Assert
        assertThat(result.hasErrors()).isTrue();
    }

    // Helper method to create test invoices
    private Invoice createTestInvoice(String id) {
        Invoice invoice = new Invoice();
        invoice.setId(id);
        invoice.setStatus(Invoice.InvoiceStatus.DRAFT);
        return invoice;
    }
}
