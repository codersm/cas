package org.apereo.cas.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.stream.IntStream;

/**
 * This is {@link RandomUtils}
 * that encapsulates common base64 calls and operations
 * in one spot.
 *
 * @author Timur Duehr timur.duehr@nccgroup.trust
 * @since 5.2.0
 */
@Slf4j
@UtilityClass
public class RandomUtils {
    private static final int HEX_HIGH_BITS_BITWISE_FLAG = 0x0f;
    private static final int SECURE_ID_CHARS_LENGTH = 40;
    private static final int SECURE_ID_BYTES_LENGTH = 20;
    private static final int SECURE_ID_SHIFT_LENGTH = 4;
    private static final String NATIVE_NON_BLOCKING_ALGORITHM = "NativePRNGNonBlocking";

    /**
     * Get strong enough SecureRandom instance and of the checked exception.
     *
     * @return the strong instance
     */
    public static SecureRandom getNativeInstance() {
        try {
            return SecureRandom.getInstance(NATIVE_NON_BLOCKING_ALGORITHM);
        } catch (final NoSuchAlgorithmException e) {
            LOGGER.trace(e.getMessage(), e);
            return new SecureRandom();
        }
    }

    /**
     * Generate secure random id string.
     *
     * @return the string
     */
    public static String generateSecureRandomId() {
        val generator = getNativeInstance();
        val charMappings = new char[]{
            'a', 'b', 'c', 'd', 'e', 'f', 'g',
            'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
            'p'};

        val bytes = new byte[SECURE_ID_BYTES_LENGTH];
        generator.nextBytes(bytes);

        val chars = new char[SECURE_ID_CHARS_LENGTH];
        IntStream.range(0, bytes.length).forEach(i -> {
            val left = bytes[i] >> SECURE_ID_SHIFT_LENGTH & HEX_HIGH_BITS_BITWISE_FLAG;
            val right = bytes[i] & HEX_HIGH_BITS_BITWISE_FLAG;
            chars[i * 2] = charMappings[left];
            chars[i * 2 + 1] = charMappings[right];
        });
        return String.valueOf(chars);
    }
}
