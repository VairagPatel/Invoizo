package in.invoizo.invoicegeneratorapi.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TransitionContext and its Builder pattern.
 * Validates Requirements: 7.1-7.11, 10.4
 */
class TransitionContextTest {

    @Test
    void testBuilderWithAllFields() {
        // Arrange
        String triggerEvent = "PAYMENT";
        Instant eventTimestamp = Instant.now();
        String razorpayPaymentId = "pay_123";
        String razorpayOrderId = "order_456";
        String razorpayPaymentLinkId = "plink_789";
        ViewerInfo viewerInfo = new ViewerInfo("192.168.1.1", "Mozilla/5.0");
        String userId = "user_001";

        // Act
        TransitionContext context = new TransitionContext.Builder()
                .triggerEvent(triggerEvent)
                .eventTimestamp(eventTimestamp)
                .razorpayPaymentId(razorpayPaymentId)
                .razorpayOrderId(razorpayOrderId)
                .razorpayPaymentLinkId(razorpayPaymentLinkId)
                .viewerInfo(viewerInfo)
                .userId(userId)
                .build();

        // Assert
        assertEquals(triggerEvent, context.getTriggerEvent());
        assertEquals(eventTimestamp, context.getEventTimestamp());
        assertEquals(razorpayPaymentId, context.getRazorpayPaymentId());
        assertEquals(razorpayOrderId, context.getRazorpayOrderId());
        assertEquals(razorpayPaymentLinkId, context.getRazorpayPaymentLinkId());
        assertEquals(viewerInfo, context.getViewerInfo());
        assertEquals(userId, context.getUserId());
    }

    @Test
    void testBuilderWithMinimalFields() {
        // Arrange
        String triggerEvent = "SEND";

        // Act
        TransitionContext context = new TransitionContext.Builder()
                .triggerEvent(triggerEvent)
                .build();

        // Assert
        assertEquals(triggerEvent, context.getTriggerEvent());
        assertNotNull(context.getEventTimestamp()); // Should default to current time
        assertNull(context.getRazorpayPaymentId());
        assertNull(context.getRazorpayOrderId());
        assertNull(context.getRazorpayPaymentLinkId());
        assertNull(context.getViewerInfo());
        assertNull(context.getUserId());
    }

    @Test
    void testBuilderForViewTransition() {
        // Arrange
        String triggerEvent = "VIEW";
        ViewerInfo viewerInfo = new ViewerInfo("10.0.0.1", "Chrome/91.0");

        // Act
        TransitionContext context = new TransitionContext.Builder()
                .triggerEvent(triggerEvent)
                .viewerInfo(viewerInfo)
                .build();

        // Assert
        assertEquals(triggerEvent, context.getTriggerEvent());
        assertEquals(viewerInfo, context.getViewerInfo());
        assertNotNull(context.getEventTimestamp());
    }

    @Test
    void testBuilderForPaymentTransition() {
        // Arrange
        String triggerEvent = "PAYMENT";
        String razorpayPaymentId = "pay_abc123";
        String razorpayOrderId = "order_xyz789";

        // Act
        TransitionContext context = new TransitionContext.Builder()
                .triggerEvent(triggerEvent)
                .razorpayPaymentId(razorpayPaymentId)
                .razorpayOrderId(razorpayOrderId)
                .build();

        // Assert
        assertEquals(triggerEvent, context.getTriggerEvent());
        assertEquals(razorpayPaymentId, context.getRazorpayPaymentId());
        assertEquals(razorpayOrderId, context.getRazorpayOrderId());
    }

    @Test
    void testBuilderForManualCancelTransition() {
        // Arrange
        String triggerEvent = "MANUAL_CANCEL";
        String userId = "clerk_user_123";

        // Act
        TransitionContext context = new TransitionContext.Builder()
                .triggerEvent(triggerEvent)
                .userId(userId)
                .build();

        // Assert
        assertEquals(triggerEvent, context.getTriggerEvent());
        assertEquals(userId, context.getUserId());
    }

    @Test
    void testBuilderForOverdueTransition() {
        // Arrange
        String triggerEvent = "OVERDUE";

        // Act
        TransitionContext context = new TransitionContext.Builder()
                .triggerEvent(triggerEvent)
                .build();

        // Assert
        assertEquals(triggerEvent, context.getTriggerEvent());
        assertNotNull(context.getEventTimestamp());
    }

    @Test
    void testEventTimestampDefaultsToNow() {
        // Act
        Instant before = Instant.now();
        TransitionContext context = new TransitionContext.Builder()
                .triggerEvent("TEST")
                .build();
        Instant after = Instant.now();

        // Assert
        assertNotNull(context.getEventTimestamp());
        assertTrue(context.getEventTimestamp().isAfter(before.minusSeconds(1)));
        assertTrue(context.getEventTimestamp().isBefore(after.plusSeconds(1)));
    }

    @Test
    void testToString() {
        // Arrange
        TransitionContext context = new TransitionContext.Builder()
                .triggerEvent("SEND")
                .razorpayPaymentLinkId("plink_123")
                .build();

        // Act
        String result = context.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("SEND"));
        assertTrue(result.contains("plink_123"));
        assertTrue(result.contains("TransitionContext"));
    }
}
