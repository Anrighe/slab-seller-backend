package service;

import lombok.Data;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.shuffle;

@Data
public class PasswordService {

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%&*()-_=+[]{}";
    private static final String ALL = UPPER + LOWER + DIGITS + SPECIAL;

    private static final int STANDARD_PASSWORD_LENGTH = 16;
    private static final int MINIMUM_PASSWORD_LENGTH = 8;

    private static final SecureRandom secureRandom = new SecureRandom();

    public static String generatePassword() throws IllegalArgumentException {
        return generatePassword(STANDARD_PASSWORD_LENGTH);
    }

    public static String generatePassword(final int passwordLength) throws IllegalArgumentException {
        if (passwordLength < MINIMUM_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Minimum password length is " + MINIMUM_PASSWORD_LENGTH);
        }

        StringBuilder passwordComponentsBuilder = new StringBuilder(passwordLength);

        // Ensure at least one character from each category
        passwordComponentsBuilder.append(UPPER.charAt(secureRandom.nextInt(UPPER.length())));
        passwordComponentsBuilder.append(LOWER.charAt(secureRandom.nextInt(LOWER.length())));
        passwordComponentsBuilder.append(DIGITS.charAt(secureRandom.nextInt(DIGITS.length())));
        passwordComponentsBuilder.append(SPECIAL.charAt(secureRandom.nextInt(SPECIAL.length())));

        StringBuilder passwordBuilder = new StringBuilder(passwordLength);

        // Fill the rest with random characters from ALL
        for (int index = 4; index < passwordLength; index++) {
            passwordComponentsBuilder.append(ALL.charAt(secureRandom.nextInt(ALL.length())));
        }

        List<Character> passwordChars = new ArrayList<>();

        for (int index = 0; index < passwordComponentsBuilder.length(); index++) {
            passwordChars.add(passwordComponentsBuilder.charAt(index));
        }

        shuffle(passwordChars);

        for (Character passwordChar : passwordChars) {
            passwordBuilder.append(passwordChar);
        }

        return passwordBuilder.toString();
    }
}
