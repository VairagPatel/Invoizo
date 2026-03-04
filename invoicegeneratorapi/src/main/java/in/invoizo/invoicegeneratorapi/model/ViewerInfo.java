package in.invoizo.invoicegeneratorapi.model;

import java.time.Instant;

/**
 * Information about a viewer accessing an invoice.
 * Used for tracking customer views and recording view events.
 * 
 * Requirement: 3.1
 */
public class ViewerInfo {
    private final String ipAddress;
    private final String userAgent;
    private final Instant viewTimestamp;

    public ViewerInfo(String ipAddress, String userAgent, Instant viewTimestamp) {
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.viewTimestamp = viewTimestamp;
    }

    public ViewerInfo(String ipAddress, String userAgent) {
        this(ipAddress, userAgent, Instant.now());
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public Instant getViewTimestamp() {
        return viewTimestamp;
    }

    @Override
    public String toString() {
        return "ViewerInfo{" +
                "ipAddress='" + ipAddress + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", viewTimestamp=" + viewTimestamp +
                '}';
    }
}
