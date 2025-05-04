package utils;

import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Slf4j
public class HashUtil {

    /**
     * Uses SHA-256 to create a unique hash by combining a randomly generated salt, the provided string, and timestamp.
     * The resulting hash is as an url friendly id by encoding in base 64
     * @param string A string value to include in the hash
     * @param time  The timestamp to include in the hash
     * @return      A base64 string representing the hashed ID
     * @throws RuntimeException If the SHA-256 algorithm is not available
     */
    public static String generateHashedUrlFriendlyId(String string, Instant time) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            String salt = UUID.randomUUID().toString();
            String toHash = String.format("%s%s%s", salt, string, time);

            byte[] hashBytes = digest.digest(toHash.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            String error = String.format("Error while generating Url Friendly Hash: %s", e.getMessage());
            log.error(error);
            throw new RuntimeException("error", e);
        }
    }
}
