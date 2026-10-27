package com.qlts.util;

import java.security.SecureRandom;

public final class OtpUtil {
    private static final SecureRandom RND = new SecureRandom();
    private OtpUtil() {}

    /** Sinh OTP 6 chữ số. */
    public static String generate() {
        return String.format("%06d", RND.nextInt(1_000_000));
    }
}