package org.routineslim.global.util;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * UUID 생성·변환 도구.
 * PK는 BINARY(16)에 UUIDv7로 저장한다 (앞 48비트가 생성 시각이라 시간순으로 정렬된다).
 */
public final class UuidUtils {

    private static final SecureRandom RANDOM = new SecureRandom();

    private UuidUtils() {
    }

    /** UUIDv7 (RFC 9562): 48비트 Unix 밀리초 + 버전 7 + 무작위 74비트 */
    public static UUID generateV7() {
        long timestamp = System.currentTimeMillis();
        byte[] random = new byte[10];
        RANDOM.nextBytes(random);

        long randA = ((random[0] & 0x0FL) << 8) | (random[1] & 0xFFL);
        long msb = (timestamp << 16) | (0x7L << 12) | randA;

        long lsb = 0;
        for (int i = 2; i < 10; i++) {
            lsb = (lsb << 8) | (random[i] & 0xFFL);
        }
        lsb = (lsb & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L; // variant 10

        return new UUID(msb, lsb);
    }

    /** 네이티브 쿼리에서 BINARY(16) 컬럼과 비교할 때 쓴다 */
    public static byte[] toBytes(UUID uuid) {
        return ByteBuffer.allocate(16)
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits())
                .array();
    }
}
