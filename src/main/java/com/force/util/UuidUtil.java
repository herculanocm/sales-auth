package com.force.util;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class UuidUtil {

    private UuidUtil() {
        // Private constructor to prevent instantiation
    }

        public static UUID generateUuidV7() {
        // Get current timestamp in milliseconds since Unix epoch
        long timestamp = System.currentTimeMillis();

        // Extract the lower 48 bits of the timestamp
        long timestamp48 = timestamp & 0xFFFFFFFFFFFFL;

        // Shift timestamp to align with bits 16-63 of MSB
        long msb = timestamp48 << 16;

        // Add version (bits 12-15 set to 0111 for version 7)
        msb |= 0x0000000000007000L;

        // Add 12 random bits to the least significant bits of MSB (bits 0-11)
        msb |= (ThreadLocalRandom.current().nextLong() & 0x0FFF);

        // Set variant bits (bits 64 and 65) to '10' and generate 62 random bits for LSB
        long lsb = (0x8000000000000000L) | (ThreadLocalRandom.current().nextLong() & 0x3FFFFFFFFFFFFFFFL);

        return new UUID(msb, lsb);
    }
    
}
