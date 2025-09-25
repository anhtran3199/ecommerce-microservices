package com.ecommerce.common.util;

public class ValidationUtil {

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isValidLength(String value, int minLength, int maxLength) {
        if (value == null) {
            return false;
        }
        int length = value.length();
        return length >= minLength && length <= maxLength;
    }

    public static boolean isPositiveNumber(Number number) {
        if (number == null) {
            return false;
        }
        return number.doubleValue() > 0;
    }

    public static boolean isNonNegativeNumber(Number number) {
        if (number == null) {
            return false;
        }
        return number.doubleValue() >= 0;
    }
}