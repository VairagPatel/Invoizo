package in.invoizo.invoicegeneratorapi.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PaymentInfo.
 * Validates Requirement: 4.2
 */
class PaymentInfoTest {

    @Test
    void testConstructorWithAllFields() {
        // Arrange
        String paymentId = "pay_123456789";
        String orderId = "order_987654321";
        String paymentLinkId = "plink_abcdef123";
        BigDecimal amount = new BigDecimal("1500.50");
        String currency = "INR";
        String status = "captured";
        Instant paidAt = Instant.now();

        // Act
        PaymentInfo paymentInfo = new PaymentInfo(
            paymentId, orderId, paymentLinkId, amount, currency, status, paidAt
        );

        // Assert
        assertEquals(paymentId, paymentInfo.getPaymentId());
        assertEquals(orderId, paymentInfo.getOrderId());
        assertEquals(paymentLinkId, paymentInfo.getPaymentLinkId());
        assertEquals(amount, paymentInfo.getAmount());
        assertEquals(currency, paymentInfo.getCurrency());
        assertEquals(status, paymentInfo.getStatus());
        assertEquals(paidAt, paymentInfo.getPaidAt());
    }

    @Test
    void testToString() {
        // Arrange
        PaymentInfo paymentInfo = new PaymentInfo(
            "pay_test123",
            "order_test456",
            "plink_test789",
            new BigDecimal("2500.00"),
            "INR",
            "captured",
            Instant.now()
        );

        // Act
        String result = paymentInfo.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("pay_test123"));
        assertTrue(result.contains("order_test456"));
        assertTrue(result.contains("plink_test789"));
        assertTrue(result.contains("2500.00"));
        assertTrue(result.contains("INR"));
        assertTrue(result.contains("captured"));
        assertTrue(result.contains("PaymentInfo"));
    }

    @Test
    void testWithNullValues() {
        // Act
        PaymentInfo paymentInfo = new PaymentInfo(
            null, null, null, null, null, null, null
        );

        // Assert
        assertNull(paymentInfo.getPaymentId());
        assertNull(paymentInfo.getOrderId());
        assertNull(paymentInfo.getPaymentLinkId());
        assertNull(paymentInfo.getAmount());
        assertNull(paymentInfo.getCurrency());
        assertNull(paymentInfo.getStatus());
        assertNull(paymentInfo.getPaidAt());
    }

    @Test
    void testWithEmptyStrings() {
        // Arrange
        String paymentId = "";
        String orderId = "";
        String paymentLinkId = "";
        BigDecimal amount = BigDecimal.ZERO;
        String currency = "";
        String status = "";
        Instant paidAt = Instant.now();

        // Act
        PaymentInfo paymentInfo = new PaymentInfo(
            paymentId, orderId, paymentLinkId, amount, currency, status, paidAt
        );

        // Assert
        assertEquals("", paymentInfo.getPaymentId());
        assertEquals("", paymentInfo.getOrderId());
        assertEquals("", paymentInfo.getPaymentLinkId());
        assertEquals(BigDecimal.ZERO, paymentInfo.getAmount());
        assertEquals("", paymentInfo.getCurrency());
        assertEquals("", paymentInfo.getStatus());
        assertEquals(paidAt, paymentInfo.getPaidAt());
    }

    @Test
    void testWithDifferentCurrencies() {
        // Test with USD
        PaymentInfo usdPayment = new PaymentInfo(
            "pay_usd", "order_usd", "plink_usd",
            new BigDecimal("100.00"), "USD", "captured", Instant.now()
        );
        assertEquals("USD", usdPayment.getCurrency());

        // Test with EUR
        PaymentInfo eurPayment = new PaymentInfo(
            "pay_eur", "order_eur", "plink_eur",
            new BigDecimal("85.50"), "EUR", "captured", Instant.now()
        );
        assertEquals("EUR", eurPayment.getCurrency());

        // Test with INR
        PaymentInfo inrPayment = new PaymentInfo(
            "pay_inr", "order_inr", "plink_inr",
            new BigDecimal("7500.00"), "INR", "captured", Instant.now()
        );
        assertEquals("INR", inrPayment.getCurrency());
    }

    @Test
    void testWithDifferentStatuses() {
        // Test with captured status
        PaymentInfo capturedPayment = new PaymentInfo(
            "pay_1", "order_1", "plink_1",
            new BigDecimal("1000.00"), "INR", "captured", Instant.now()
        );
        assertEquals("captured", capturedPayment.getStatus());

        // Test with failed status
        PaymentInfo failedPayment = new PaymentInfo(
            "pay_2", "order_2", "plink_2",
            new BigDecimal("1000.00"), "INR", "failed", Instant.now()
        );
        assertEquals("failed", failedPayment.getStatus());

        // Test with authorized status
        PaymentInfo authorizedPayment = new PaymentInfo(
            "pay_3", "order_3", "plink_3",
            new BigDecimal("1000.00"), "INR", "authorized", Instant.now()
        );
        assertEquals("authorized", authorizedPayment.getStatus());
    }

    @Test
    void testAmountPrecision() {
        // Test with high precision amount
        BigDecimal preciseAmount = new BigDecimal("1234.5678");
        PaymentInfo paymentInfo = new PaymentInfo(
            "pay_precise", "order_precise", "plink_precise",
            preciseAmount, "INR", "captured", Instant.now()
        );

        assertEquals(preciseAmount, paymentInfo.getAmount());
        assertEquals(0, preciseAmount.compareTo(paymentInfo.getAmount()));
    }

    @Test
    void testTimestampAccuracy() {
        // Arrange
        Instant specificTime = Instant.parse("2024-01-15T10:30:00Z");

        // Act
        PaymentInfo paymentInfo = new PaymentInfo(
            "pay_time", "order_time", "plink_time",
            new BigDecimal("500.00"), "INR", "captured", specificTime
        );

        // Assert
        assertEquals(specificTime, paymentInfo.getPaidAt());
    }
}
