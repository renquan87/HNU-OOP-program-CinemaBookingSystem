package com.cinema.config;

import java.util.Properties;

/**
 * Consolidates the places where the application attempts to resolve the database password.
 */
public final class DbPasswordResolver {
    private static final String ENV_KEY = "DB_PASSWORD";

    private DbPasswordResolver() {
        // Utility class
    }

    /**
     * Reads the password from the {@code DB_PASSWORD} environment variable.
     */
    public static String fromEnvironment() {
        return trimToNull(System.getenv(ENV_KEY));
    }

    /**
     * Loads the password from the first command-line argument.
     */
    public static String fromCommandLine(String[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        return trimToNull(args[0]);
    }

    /**
     * Retrieves the password from a {@code config.properties} {@link Properties} instance.
     */
    public static String fromProperties(Properties props) {
        if (props == null) {
            return null;
        }
        return trimToNull(props.getProperty("db.password"));
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
