package org.example.utility;

import java.util.Random;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class containing helper methods for generating random IDs and calculating hashes.
 */
public class Utils {

    /**
     * Generates a random 6-digit ID in the range [100000, 999999].
     *
     * @return a randomly generated integer ID
     */
    public static int generateRandomID() {
        Random random = new Random(); // Create a Random object to generate random numbers
        return 100000 + random.nextInt(900000); // Generate a random number between 100000 and 999999
    }

    /**
     * Calculates the SHA-256 hash of the given input string.
     *
     * @param input the string to hash
     * @return the hexadecimal representation of the SHA-256 hash
     * @throws RuntimeException if the SHA-256 algorithm is not available
     */
    public static String calculateHash(String input) {
        try {
            // Get an instance of the SHA-256 message digest algorithm
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Compute the hash of the input string as a byte array
            byte[] hashBytes = digest.digest(input.getBytes());

            // Convert the byte array to a hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b); // Convert each byte to a 2-digit hexadecimal value
                if (hex.length() == 1) {
                    hexString.append('0'); // Pad single-digit hex values with a leading zero
                }
                hexString.append(hex);
            }
            return hexString.toString(); // Return the complete hexadecimal string
        } catch (NoSuchAlgorithmException e) {
            // Throw a runtime exception if the SHA-256 algorithm is unavailable
            throw new RuntimeException("Error calculating hash", e);
        }
    }
}