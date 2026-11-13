package com.training.assetmanagement.auth.domain;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Mật khẩu tạm 12 ký tự, luôn khớp PasswordPolicy.PATTERN
public final class TemporaryPasswordGenerator {

    public static final int LENGTH = 12;

    // Bỏ ký tự dễ nhầm: 0 O 1 l I
    private static final String LOWERCASE = "abcdefghijkmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String DIGITS = "23456789";
    // Bỏ ký tự đặc biệt của HTML: < > & " '
    private static final String SPECIALS = "!#$%*+-=?@^_~";
    private static final String ALL = LOWERCASE + UPPERCASE + DIGITS + SPECIALS;

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate() {
        List<Character> characters = new ArrayList<>(LENGTH);
        // Mỗi nhóm ít nhất một ký tự
        characters.add(pick(LOWERCASE));
        characters.add(pick(UPPERCASE));
        characters.add(pick(DIGITS));
        characters.add(pick(SPECIALS));
        while (characters.size() < LENGTH) {
            characters.add(pick(ALL));
        }
        Collections.shuffle(characters, RANDOM);
        StringBuilder password = new StringBuilder(LENGTH);
        characters.forEach(password::append);
        return password.toString();
    }

    private static char pick(String source) {
        return source.charAt(RANDOM.nextInt(source.length()));
    }

    private TemporaryPasswordGenerator() {}
}
