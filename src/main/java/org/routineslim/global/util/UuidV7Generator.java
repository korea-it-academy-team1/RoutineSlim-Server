package org.routineslim.global.util;

import java.security.SecureRandom;
import java.util.UUID;

public class UuidV7Generator {

    private static final SecureRandom RANDOM = new SecureRandom(); // 난수 생성기 준비

    public static UUID generate() {

        // 현재 시간 가져오기 (밀리초 단위로 반환)
        long timestamp = System.currentTimeMillis();

        // UUID 앞부분 (UUIDv7의 앞 64비트: 시간 + 버전 + 난수)
        long mostSignificantBits =
                ((timestamp & 0xFFFFFFFFFFFFL) << 16)
                        | 0x7000L
                        | RANDOM.nextInt(4096);

        // UUID 뒤부분 (UUID의 뒤 64비트: variant + 난수)
        long leastSignificantBits =
                (RANDOM.nextLong() & 0x3FFFFFFFFFFFFFFFL)
                        | 0x8000000000000000L;

        return new UUID(mostSignificantBits, leastSignificantBits);
    }
}
