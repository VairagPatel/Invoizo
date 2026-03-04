package in.invoizo.invoicegeneratorapi.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Information about a payment received from Razorpay.
 * Used for processing payment webhooks and updating invoice status.
 * 
 * Requirement: 4.2
 */
public class PaymentInfo {
    private final String paymentId;
    private final String orderId;
    private final String paymentLinkId;
    private final BigDecimal amount;
    private final String currency;
    private final String status;
    private final Instant paidAt;

    public PaymentInfo(String paymentId, String orderId, String paymentLinkId, 
                       BigDecimal amount, String currency, String status, Instant paidAt) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.paymentLinkId = paymentLinkId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.paidAt = paidAt;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getPaymentLinkId() {
        return paymentLinkId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getStatus() {
        return status;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    @Override
    public String toString() {
        return "PaymentInfo{" +
                "paymentId='" + paymentId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", paymentLinkId='" + paymentLinkId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", status='" + status + '\'' +
                ", paidAt=" + paidAt +
                '}';
    }
}
