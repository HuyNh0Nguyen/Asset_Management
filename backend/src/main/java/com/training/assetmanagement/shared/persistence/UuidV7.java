package com.training.assetmanagement.shared.persistence;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.UUID;

// UUIDv7 (RFC 9562): 48 bit thời gian ms + version 7 + 74 bit ngẫu nhiên
public final class UuidV7 {

    private static final SecureRandom RANDOM = new SecureRandom();

    public static UUID generate(Clock clock) {
        long mostSigBits = (clock.millis() << 16) | 0x7000L | (RANDOM.nextInt() & 0x0FFFL);
        long leastSigBits = (RANDOM.nextLong() & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L;
        return new UUID(mostSigBits, leastSigBits);
    }

    private UuidV7() {}
}
