package com.personalfinance.contador.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DatabaseHelper {
    private static final String DB_NAME = "contador_gastos.db";
    private static final String CONNECTION_URL = "jdbc:sqlite:" + DB_NAME;

    static {
        // Explicitly load the SQLite JDBC driver
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Error loading the SQLite JDBC driver: " + e.getMessage());
        }
    }

    /**
     * Returns a new connection to the SQLite database.
     * The database is stored in the project root (execution directory).
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(CONNECTION_URL);
    }

    /**
     * Initializes the database by executing the init.sql script if necessary.
     */
    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Load the init.sql script from resources
            InputStream is = DatabaseHelper.class.getResourceAsStream("/db/init.sql");
            if (is == null) {
                throw new RuntimeException("Initialization script init.sql not found in resources.");
            }

            String scriptContent;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
                scriptContent = reader.lines().collect(Collectors.joining("\n"));
            }

            // Split the script into individual statements by semicolon (;)
            // This avoids issues executing multiple statements in a single call in SQLite JDBC
            String[] sqlStatements = scriptContent.split(";");
            for (String sql : sqlStatements) {
                String cleanSql = sql.trim();
                if (!cleanSql.isEmpty()) {
                    stmt.execute(cleanSql);
                }
            }
            System.out.println("Database initialized successfully (Tables and Indexes validated).");
        } catch (Exception e) {
            System.err.println("Error initializing the SQLite database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
