package com.ecommerce.common.constants;

public class AppConstants {

    // JWT Constants
    public static final String JWT_BEARER_PREFIX = "Bearer ";
    public static final String JWT_HEADER_NAME = "Authorization";
    public static final String JWT_USERNAME_CLAIM = "username";
    public static final String JWT_EMAIL_CLAIM = "email";

    // HTTP Headers
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USERNAME_HEADER = "X-Username";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    // Date Formats
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    private AppConstants() {
        // Utility class
    }
}