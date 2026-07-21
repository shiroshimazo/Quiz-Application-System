package quizapplicationsystem.shared.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Verifies the salted PBKDF2 password hashes stored in the users table. */
final class PasswordHasher {

    private static final String HASH_NAME = "pbkdf2_sha256";
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int HASH_PART_COUNT = 4;
    private static final int MINIMUM_ITERATIONS = 100_000;
    private static final int MAXIMUM_ITERATIONS = 2_000_000;
    private static final int MINIMUM_SALT_BYTES = 16;

    private PasswordHasher() {
    }

    static boolean matches(String password, String encodedHash) {
        if (password == null || encodedHash == null) {
            return false;
        }

        String[] parts = encodedHash.split("\\$", -1);
        if (parts.length != HASH_PART_COUNT || !HASH_NAME.equals(parts[0])) {
            return false;
        }

        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);

            if (iterations < MINIMUM_ITERATIONS
                    || iterations > MAXIMUM_ITERATIONS
                    || salt.length < MINIMUM_SALT_BYTES
                    || expectedHash.length == 0) {
                return false;
            }

            PBEKeySpec keySpec = new PBEKeySpec(
                    password.toCharArray(), salt, iterations, expectedHash.length * Byte.SIZE);
            byte[] actualHash = null;

            try {
                SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
                actualHash = keyFactory.generateSecret(keySpec).getEncoded();
                return MessageDigest.isEqual(actualHash, expectedHash);
            } finally {
                keySpec.clearPassword();
                if (actualHash != null) {
                    Arrays.fill(actualHash, (byte) 0);
                }
            }
        } catch (IllegalArgumentException exception) {
            return false;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException exception) {
            throw new IllegalStateException("PBKDF2 password verification is unavailable.", exception);
        }
    }
}
