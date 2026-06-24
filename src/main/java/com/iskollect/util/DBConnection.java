package com.iskollect.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class DBConnection {
    private static final Map<String, String> LOCAL_VALUES = loadLocalValues();

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            required("ISKOLLECT_DB_URL"),
            required("ISKOLLECT_DB_USER"),
            required("ISKOLLECT_DB_PASSWORD")
        );
    }

    private static String required(String name) throws SQLException {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            value = LOCAL_VALUES.get(name);
        }
        if (value == null || value.isBlank()) {
            throw new SQLException(
                "Missing " + name
                    + ". Set the environment variable or add it to the local .env file."
            );
        }
        return value.trim();
    }

    private static Map<String, String> loadLocalValues() {
        Path file = Path.of(".env").toAbsolutePath().normalize();
        if (!Files.isRegularFile(file)) {
            return Map.of();
        }
        Map<String, String> values = new HashMap<>();
        try {
            for (String line : Files.readAllLines(file)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int separator = trimmed.indexOf('=');
                if (separator > 0) {
                    values.put(
                        trimmed.substring(0, separator).trim(),
                        trimmed.substring(separator + 1).trim()
                    );
                }
            }
            return Map.copyOf(values);
        } catch (IOException exception) {
            return Map.of();
        }
    }
}
