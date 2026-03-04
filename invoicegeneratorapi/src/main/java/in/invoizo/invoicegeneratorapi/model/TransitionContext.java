package in.invoizo.invoicegeneratorapi.model;

import java.time.Instant;

/**
 * Context object containing information about a status transition event.
 * Used by the StatusTransitionEngine to record audit trails and update appropriate fields.
 * 
 * Requirements: 7.1-7.11, 10.4
 */
public class TransitionContext {
    private final String triggerEvent;
    private final Instant eventTimestamp;
    private final String razorpayPaymentId;
    private final String razorpayOrderId;
    private final String razorpayPaymentLinkId;
    private final ViewerInfo viewerInfo;
    private final String userId;

    private TransitionContext(Builder builder) {
        this.triggerEvent = builder.triggerEvent;
        this.eventTimestamp = builder.eventTimestamp;
        this.razorpayPaymentId = builder.razorpayPaymentId;
        this.razorpayOrderId = builder.razorpayOrderId;
        this.razorpayPaymentLinkId = builder.razorpayPaymentLinkId;
        this.viewerInfo = builder.viewerInfo;
        this.userId = builder.userId;
    }

    public String getTriggerEvent() {
        return triggerEvent;
    }

    public Instant getEventTimestamp() {
        return eventTimestamp;
    }

    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public String getRazorpayPaymentLinkId() {
        return razorpayPaymentLinkId;
    }

    public ViewerInfo getViewerInfo() {
        return viewerInfo;
    }

    public String getUserId() {
        return userId;
    }

    /**
     * Builder for constructing TransitionContext instances.
     * Provides a flexible way to create context objects with only the required fields.
     */
    public static class Builder {
        private String triggerEvent;
        private Instant eventTimestamp;
        private String razorpayPaymentId;
        private String razorpayOrderId;
        private String razorpayPaymentLinkId;
        private ViewerInfo viewerInfo;
        private String userId;

        public Builder() {
            this.eventTimestamp = Instant.now();
        }

        /**
         * Sets the trigger event type.
         * Common values: "SEND", "VIEW", "PAYMENT", "OVERDUE", "MANUAL_CANCEL"
         */
        public Builder triggerEvent(String triggerEvent) {
            this.triggerEvent = triggerEvent;
            return this;
        }

        /**
         * Sets the event timestamp. Defaults to current time if not set.
         */
        public Builder eventTimestamp(Instant eventTimestamp) {
            this.eventTimestamp = eventTimestamp;
            return this;
        }

        /**
         * Sets the Razorpay payment ID (for payment transitions).
         */
        public Builder razorpayPaymentId(String razorpayPaymentId) {
            this.razorpayPaymentId = razorpayPaymentId;
            return this;
        }

        /**
         * Sets the Razorpay order ID (for payment transitions).
         */
        public Builder razorpayOrderId(String razorpayOrderId) {
            this.razorpayOrderId = razorpayOrderId;
            return this;
        }

        /**
         * Sets the Razorpay payment link ID (for send and payment transitions).
         */
        public Builder razorpayPaymentLinkId(String razorpayPaymentLinkId) {
            this.razorpayPaymentLinkId = razorpayPaymentLinkId;
            return this;
        }

        /**
         * Sets the viewer information (for view transitions).
         */
        public Builder viewerInfo(ViewerInfo viewerInfo) {
            this.viewerInfo = viewerInfo;
            return this;
        }

        /**
         * Sets the user ID (for manual actions like cancel).
         */
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        /**
         * Builds the TransitionContext instance.
         */
        public TransitionContext build() {
            return new TransitionContext(this);
        }
    }

    @Override
    public String toString() {
        return "TransitionContext{" +
                "triggerEvent='" + triggerEvent + '\'' +
                ", eventTimestamp=" + eventTimestamp +
                ", razorpayPaymentId='" + razorpayPaymentId + '\'' +
                ", razorpayOrderId='" + razorpayOrderId + '\'' +
                ", razorpayPaymentLinkId='" + razorpayPaymentLinkId + '\'' +
                ", viewerInfo=" + viewerInfo +
                ", userId='" + userId + '\'' +
                '}';
    }
}
