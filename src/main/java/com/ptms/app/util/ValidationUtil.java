package com.ptms.app.util;

import java.util.regex.Pattern;

public final class ValidationUtil {

    private ValidationUtil() {
    }

    private static final Pattern NAME =
            Pattern.compile("^[A-Za-z ]{2,50}$");

    private static final Pattern USERNAME =
            Pattern.compile("^[A-Za-z0-9_]{4,30}$");

    private static final Pattern EMAIL =
            Pattern.compile(
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
            );

    private static final Pattern PHONE =
            Pattern.compile("^[6-9][0-9]{9}$");

    public static boolean isBlank(String value) {
        return value == null ||
                value.trim().isEmpty();
    }

    public static boolean isValidName(String value) {
        return value != null &&
                NAME.matcher(value.trim()).matches();
    }

    public static boolean isValidUsername(String value) {
        return value != null &&
                USERNAME.matcher(value.trim()).matches();
    }

    public static boolean isValidEmail(String value) {
        return value != null &&
                EMAIL.matcher(value.trim()).matches();
    }

    public static boolean isValidPhone(String value) {
        return value != null &&
                PHONE.matcher(value.trim()).matches();
    }

    public static boolean isValidPassword(String value) {
        return value != null &&
                value.length() >= 8;
    }
}