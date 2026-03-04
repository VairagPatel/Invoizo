package in.invoizo.invoicegeneratorapi.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ViewerInfo.
 * Validates Requirement: 3.1
 */
class ViewerInfoTest {

    @Test
    void testConstructorWithAllFields() {
        // Arrange
        String ipAddress = "192.168.1.100";
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";
        Instant viewTimestamp = Instant.now();

        // Act
        ViewerInfo viewerInfo = new ViewerInfo(ipAddress, userAgent, viewTimestamp);

        // Assert
        assertEquals(ipAddress, viewerInfo.getIpAddress());
        assertEquals(userAgent, viewerInfo.getUserAgent());
        assertEquals(viewTimestamp, viewerInfo.getViewTimestamp());
    }

    @Test
    void testConstructorWithDefaultTimestamp() {
        // Arrange
        String ipAddress = "10.0.0.1";
        String userAgent = "Chrome/91.0.4472.124";

        // Act
        Instant before = Instant.now();
        ViewerInfo viewerInfo = new ViewerInfo(ipAddress, userAgent);
        Instant after = Instant.now();

        // Assert
        assertEquals(ipAddress, viewerInfo.getIpAddress());
        assertEquals(userAgent, viewerInfo.getUserAgent());
        assertNotNull(viewerInfo.getViewTimestamp());
        assertTrue(viewerInfo.getViewTimestamp().isAfter(before.minusSeconds(1)));
        assertTrue(viewerInfo.getViewTimestamp().isBefore(after.plusSeconds(1)));
    }

    @Test
    void testToString() {
        // Arrange
        ViewerInfo viewerInfo = new ViewerInfo("127.0.0.1", "Safari/14.0");

        // Act
        String result = viewerInfo.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("127.0.0.1"));
        assertTrue(result.contains("Safari/14.0"));
        assertTrue(result.contains("ViewerInfo"));
    }

    @Test
    void testWithNullValues() {
        // Act
        ViewerInfo viewerInfo = new ViewerInfo(null, null);

        // Assert
        assertNull(viewerInfo.getIpAddress());
        assertNull(viewerInfo.getUserAgent());
        assertNotNull(viewerInfo.getViewTimestamp()); // Timestamp should still be set
    }

    @Test
    void testWithEmptyStrings() {
        // Arrange
        String ipAddress = "";
        String userAgent = "";

        // Act
        ViewerInfo viewerInfo = new ViewerInfo(ipAddress, userAgent);

        // Assert
        assertEquals("", viewerInfo.getIpAddress());
        assertEquals("", viewerInfo.getUserAgent());
        assertNotNull(viewerInfo.getViewTimestamp());
    }
}
