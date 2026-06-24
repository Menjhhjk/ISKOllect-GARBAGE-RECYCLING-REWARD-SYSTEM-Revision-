package com.iskollect;

import com.iskollect.util.DBConnection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.Set;

public final class TestDatabaseConnection {
    private static final Set<String> REQUIRED_TABLES = Set.of(
        "users",
        "students",
        "bottle_records",
        "points_ledger",
        "rewards_catalog",
        "redemptions"
    );

    private TestDatabaseConnection() {
    }

    public static void main(String[] args) {
        System.out.println("ISKOllect local database connection test");
        System.out.println("----------------------------------------");

        try (Connection connection = DBConnection.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();

            System.out.println("[PASS] Connected successfully");
            System.out.println("Database URL : " + metadata.getURL());
            System.out.println("Database     : " + connection.getCatalog());
            System.out.println("DB product   : " + metadata.getDatabaseProductName()
                + " " + metadata.getDatabaseProductVersion());
            System.out.println("JDBC driver  : " + metadata.getDriverName()
                + " " + metadata.getDriverVersion());
            System.out.println("DB user      : " + metadata.getUserName());

            Set<String> existingTables = findRequiredTables(connection);
            Set<String> missingTables = new LinkedHashSet<>(REQUIRED_TABLES);
            missingTables.removeAll(existingTables);

            if (missingTables.isEmpty()) {
                System.out.println("[PASS] All 6 required ISKOllect tables exist");
            } else {
                System.out.println("[FAIL] Missing tables: " + String.join(", ", missingTables));
            }

            Set<String> rewardColumns = findRewardColumns(connection);
            boolean rewardSchemaComplete = rewardColumns.contains("description")
                && rewardColumns.contains("available");
            if (rewardSchemaComplete) {
                System.out.println(
                    "[PASS] Reward description and availability columns exist"
                );
            } else {
                System.out.println(
                    "[FAIL] The rewards_catalog table does not match the current schema"
                );
                System.out.println(
                    "       Initialize a new empty database using sql/iskollect_schema.sql"
                );
            }

            if (missingTables.isEmpty()) {
                printRowCounts(connection);
            } else {
                System.out.println(
                    "Row counts skipped because required tables are missing."
                );
            }

            if (!missingTables.isEmpty() || !rewardSchemaComplete) {
                System.exit(2);
            }

            System.out.println("----------------------------------------");
            System.out.println("Database connection and schema check passed.");
        } catch (Exception exception) {
            System.err.println("[FAIL] Database connection failed");
            System.err.println(exception.getMessage());
            System.err.println();
            System.err.println("Expected local configuration:");
            System.err.println(
                "ISKOLLECT_DB_URL=jdbc:postgresql://localhost:5432/iskollect_db"
            );
            System.err.println("ISKOLLECT_DB_USER=postgres");
            System.err.println("ISKOLLECT_DB_PASSWORD=<your PostgreSQL password>");
            System.exit(1);
        }
    }

    private static Set<String> findRequiredTables(Connection connection) throws Exception {
        Set<String> tables = new LinkedHashSet<>();
        String sql = """
            SELECT table_name
            FROM information_schema.tables
            WHERE table_schema = 'public'
              AND table_name IN (
                'users', 'students', 'bottle_records',
                'points_ledger', 'rewards_catalog', 'redemptions'
              )
            ORDER BY table_name
            """;
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                tables.add(result.getString("table_name"));
            }
        }
        return tables;
    }

    private static Set<String> findRewardColumns(Connection connection) throws Exception {
        Set<String> columns = new LinkedHashSet<>();
        String sql = """
            SELECT column_name
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'rewards_catalog'
            """;
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                columns.add(result.getString("column_name"));
            }
        }
        return columns;
    }

    private static void printRowCounts(Connection connection) throws Exception {
        System.out.println("Current records:");
        for (String table : REQUIRED_TABLES.stream().sorted().toList()) {
            String sql = "SELECT COUNT(*) FROM " + table;
            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery(sql)) {
                result.next();
                System.out.printf("  %-18s %d%n", table, result.getInt(1));
            }
        }
    }
}
