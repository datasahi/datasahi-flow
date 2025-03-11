package datasahi.flow.util;

import java.time.LocalDateTime;
import java.nio.ByteBuffer;

public class LocalDateTimeConverter {

    // Convert LocalDateTime to bytes
    public static byte[] toBytes(LocalDateTime dateTime) {
        // Allocate 16 bytes: 4 (year) + 1 (month) + 1 (day) + 1 (hour) + 1 (minute) + 1 (second) + 4 (nano)
        // Plus padding to ensure proper alignment
        ByteBuffer buffer = ByteBuffer.allocate(16);

        buffer.putInt(dateTime.getYear());
        buffer.put((byte) dateTime.getMonthValue());
        buffer.put((byte) dateTime.getDayOfMonth());
        buffer.put((byte) dateTime.getHour());
        buffer.put((byte) dateTime.getMinute());
        buffer.put((byte) dateTime.getSecond());
        buffer.putInt(dateTime.getNano());  // 4 bytes for nanoseconds

        return buffer.array();
    }

    // Convert bytes back to LocalDateTime
    public static LocalDateTime fromBytes(byte[] bytes) {
        if (bytes == null || bytes.length != 16) {
            throw new IllegalArgumentException("Invalid byte array. Expected 16 bytes, got " +
                    (bytes == null ? "null" : bytes.length));
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        try {
            int year = buffer.getInt();
            int month = buffer.get() & 0xFF;
            int day = buffer.get() & 0xFF;
            int hour = buffer.get() & 0xFF;
            int minute = buffer.get() & 0xFF;
            int second = buffer.get() & 0xFF;
            int nano = buffer.getInt();

            return LocalDateTime.of(year, month, day, hour, minute, second, nano);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to convert bytes to LocalDateTime", e);
        }
    }

    // Example usage
    public static void main(String[] args) {
        LocalDateTime now = LocalDateTime.now();
        System.out.println("Original DateTime: " + now);

        // Convert to bytes
        byte[] bytes = toBytes(now);
        System.out.println("Byte array length: " + bytes.length);

        // Convert back to LocalDateTime
        LocalDateTime converted = fromBytes(bytes);
        System.out.println("Converted DateTime: " + converted);

        // Verify they are equal
        System.out.println("Are equal: " + now.equals(converted));
    }
}