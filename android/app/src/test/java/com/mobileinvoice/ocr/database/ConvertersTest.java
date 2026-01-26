package com.mobileinvoice.ocr.database;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

/**
 * Unit tests for Room type converters
 * Tests the bidirectional conversion between Long and String
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest = Config.NONE)
public class ConvertersTest {

    // ==================== fromLong Tests ====================

    @Test
    public void testFromLong_positiveValue() {
        Long input = 12345L;
        String result = Converters.fromLong(input);
        assertEquals("12345", result);
    }

    @Test
    public void testFromLong_zero() {
        Long input = 0L;
        String result = Converters.fromLong(input);
        assertEquals("0", result);
    }

    @Test
    public void testFromLong_negativeValue() {
        Long input = -12345L;
        String result = Converters.fromLong(input);
        assertEquals("-12345", result);
    }

    @Test
    public void testFromLong_null() {
        String result = Converters.fromLong(null);
        assertNull(result);
    }

    @Test
    public void testFromLong_maxValue() {
        Long input = Long.MAX_VALUE;
        String result = Converters.fromLong(input);
        assertEquals(String.valueOf(Long.MAX_VALUE), result);
    }

    @Test
    public void testFromLong_minValue() {
        Long input = Long.MIN_VALUE;
        String result = Converters.fromLong(input);
        assertEquals(String.valueOf(Long.MIN_VALUE), result);
    }

    @Test
    public void testFromLong_timestamp() {
        Long timestamp = 1704067200000L; // Jan 1, 2024 00:00:00 UTC
        String result = Converters.fromLong(timestamp);
        assertEquals("1704067200000", result);
    }

    // ==================== toLong Tests ====================

    @Test
    public void testToLong_positiveValue() {
        String input = "12345";
        Long result = Converters.toLong(input);
        assertEquals(Long.valueOf(12345L), result);
    }

    @Test
    public void testToLong_zero() {
        String input = "0";
        Long result = Converters.toLong(input);
        assertEquals(Long.valueOf(0L), result);
    }

    @Test
    public void testToLong_negativeValue() {
        String input = "-12345";
        Long result = Converters.toLong(input);
        assertEquals(Long.valueOf(-12345L), result);
    }

    @Test
    public void testToLong_null() {
        Long result = Converters.toLong(null);
        assertNull(result);
    }

    @Test
    public void testToLong_maxValue() {
        String input = String.valueOf(Long.MAX_VALUE);
        Long result = Converters.toLong(input);
        assertEquals(Long.valueOf(Long.MAX_VALUE), result);
    }

    @Test
    public void testToLong_minValue() {
        String input = String.valueOf(Long.MIN_VALUE);
        Long result = Converters.toLong(input);
        assertEquals(Long.valueOf(Long.MIN_VALUE), result);
    }

    @Test
    public void testToLong_timestamp() {
        String input = "1704067200000";
        Long result = Converters.toLong(input);
        assertEquals(Long.valueOf(1704067200000L), result);
    }

    // ==================== Round Trip Tests ====================

    @Test
    public void testRoundTrip_positiveValue() {
        Long original = 12345L;
        String intermediate = Converters.fromLong(original);
        Long result = Converters.toLong(intermediate);
        assertEquals(original, result);
    }

    @Test
    public void testRoundTrip_negativeValue() {
        Long original = -67890L;
        String intermediate = Converters.fromLong(original);
        Long result = Converters.toLong(intermediate);
        assertEquals(original, result);
    }

    @Test
    public void testRoundTrip_zero() {
        Long original = 0L;
        String intermediate = Converters.fromLong(original);
        Long result = Converters.toLong(intermediate);
        assertEquals(original, result);
    }

    @Test
    public void testRoundTrip_null() {
        Long original = null;
        String intermediate = Converters.fromLong(original);
        Long result = Converters.toLong(intermediate);
        assertEquals(original, result);
    }

    @Test
    public void testRoundTrip_maxValue() {
        Long original = Long.MAX_VALUE;
        String intermediate = Converters.fromLong(original);
        Long result = Converters.toLong(intermediate);
        assertEquals(original, result);
    }

    @Test
    public void testRoundTrip_minValue() {
        Long original = Long.MIN_VALUE;
        String intermediate = Converters.fromLong(original);
        Long result = Converters.toLong(intermediate);
        assertEquals(original, result);
    }

    @Test
    public void testRoundTrip_currentTimestamp() {
        Long original = System.currentTimeMillis();
        String intermediate = Converters.fromLong(original);
        Long result = Converters.toLong(intermediate);
        assertEquals(original, result);
    }

    // ==================== Reverse Round Trip Tests ====================

    @Test
    public void testReverseRoundTrip_positiveValue() {
        String original = "98765";
        Long intermediate = Converters.toLong(original);
        String result = Converters.fromLong(intermediate);
        assertEquals(original, result);
    }

    @Test
    public void testReverseRoundTrip_negativeValue() {
        String original = "-54321";
        Long intermediate = Converters.toLong(original);
        String result = Converters.fromLong(intermediate);
        assertEquals(original, result);
    }

    @Test
    public void testReverseRoundTrip_zero() {
        String original = "0";
        Long intermediate = Converters.toLong(original);
        String result = Converters.fromLong(intermediate);
        assertEquals(original, result);
    }

    // ==================== Edge Cases ====================

    @Test(expected = NumberFormatException.class)
    public void testToLong_invalidInput_letters() {
        Converters.toLong("abc");
    }

    @Test(expected = NumberFormatException.class)
    public void testToLong_invalidInput_empty() {
        Converters.toLong("");
    }

    @Test(expected = NumberFormatException.class)
    public void testToLong_invalidInput_mixed() {
        Converters.toLong("123abc");
    }

    @Test(expected = NumberFormatException.class)
    public void testToLong_invalidInput_decimal() {
        Converters.toLong("123.45");
    }

    @Test(expected = NumberFormatException.class)
    public void testToLong_invalidInput_whitespace() {
        Converters.toLong("  ");
    }

    @Test(expected = NumberFormatException.class)
    public void testToLong_overflow() {
        // This number is larger than Long.MAX_VALUE
        Converters.toLong("99999999999999999999");
    }

    // ==================== Consistency Tests ====================

    @Test
    public void testConsistency_multipleConversions() {
        Long value = 1234567890L;

        for (int i = 0; i < 100; i++) {
            String str = Converters.fromLong(value);
            Long back = Converters.toLong(str);
            assertEquals("Conversion should be consistent", value, back);
        }
    }

    @Test
    public void testConsistency_differentValues() {
        long[] testValues = {0L, 1L, -1L, 100L, -100L, Long.MAX_VALUE, Long.MIN_VALUE,
                            System.currentTimeMillis()};

        for (long value : testValues) {
            Long boxed = value;
            String str = Converters.fromLong(boxed);
            Long result = Converters.toLong(str);
            assertEquals("Value " + value + " should round-trip correctly", boxed, result);
        }
    }

    // ==================== Type Safety Tests ====================

    @Test
    public void testFromLong_returnsString() {
        Object result = Converters.fromLong(123L);
        assertTrue("Result should be a String", result instanceof String);
    }

    @Test
    public void testToLong_returnsLong() {
        Object result = Converters.toLong("123");
        assertTrue("Result should be a Long", result instanceof Long);
    }

    // ==================== Practical Usage Tests ====================

    @Test
    public void testConversion_forDatabaseTimestamp() {
        // Simulate storing and retrieving a timestamp
        long currentTime = System.currentTimeMillis();

        // Store as String (as it would be in older database versions)
        String stored = Converters.fromLong(currentTime);

        // Retrieve as Long
        Long retrieved = Converters.toLong(stored);

        assertEquals(Long.valueOf(currentTime), retrieved);
    }

    @Test
    public void testConversion_forInvoiceId() {
        // Simulate invoice ID conversion
        Long invoiceId = 12345L;

        String stored = Converters.fromLong(invoiceId);
        assertEquals("12345", stored);

        Long retrieved = Converters.toLong(stored);
        assertEquals(invoiceId, retrieved);
    }
}
