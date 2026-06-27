package com.personalfinance.contador.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
        // 1. Verificar y realizar la migración si es necesario
        try (Connection conn = getConnection()) {
            boolean isOldSchema = false;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT priority FROM savings LIMIT 1")) {
                isOldSchema = true;
            } catch (SQLException e) {
                // La columna priority no existe, o la tabla savings no existe
            }

            if (isOldSchema) {
                System.out.println("Old savings schema detected. Migrating to new schema...");
                try (Statement stmt = conn.createStatement()) {
                    // Renombrar tabla vieja
                    stmt.execute("ALTER TABLE savings RENAME TO savings_old");

                    // Crear nuevas tablas
                    stmt.execute("CREATE TABLE savings (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "name TEXT NOT NULL, " +
                            "description TEXT, " +
                            "target_value REAL NOT NULL CHECK (target_value >= 0), " +
                            "dateCurrent TEXT NOT NULL, " +
                            "status TEXT NOT NULL DEFAULT 'Activo'" +
                            ")");

                    stmt.execute("CREATE TABLE savings_movements (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "saving_id INTEGER NOT NULL, " +
                            "amount REAL NOT NULL CHECK (amount > 0), " +
                            "date TEXT NOT NULL, " +
                            "observation TEXT, " +
                            "FOREIGN KEY (saving_id) REFERENCES savings(id) ON DELETE CASCADE" +
                            ")");

                    // Copiar datos
                    try (Statement selectStmt = conn.createStatement();
                         ResultSet rsOld = selectStmt.executeQuery("SELECT id, dateCurrent, description, amount FROM savings_old")) {
                        
                        String insertSavingSql = "INSERT INTO savings (id, name, description, target_value, dateCurrent, status) VALUES (?, ?, ?, ?, ?, ?)";
                        String insertMovementSql = "INSERT INTO savings_movements (saving_id, amount, date, observation) VALUES (?, ?, ?, ?)";
                        
                        try (PreparedStatement psSaving = conn.prepareStatement(insertSavingSql);
                             PreparedStatement psMovement = conn.prepareStatement(insertMovementSql)) {
                            
                            while (rsOld.next()) {
                                int id = rsOld.getInt("id");
                                String date = rsOld.getString("dateCurrent");
                                String description = rsOld.getString("description");
                                double amount = rsOld.getDouble("amount");

                                // Insertar ahorro
                                psSaving.setInt(1, id);
                                psSaving.setString(2, description); // Nombre recibe la descripción anterior
                                psSaving.setString(3, "Migrado de versión anterior"); // Descripción
                                psSaving.setDouble(4, amount); // Valor objetivo recibe el monto viejo
                                psSaving.setString(5, date);
                                psSaving.setString(6, "Completado");
                                psSaving.executeUpdate();

                                // Insertar movimiento si el monto es mayor a cero
                                if (amount > 0) {
                                    psMovement.setInt(1, id);
                                    psMovement.setDouble(2, amount);
                                    psMovement.setString(3, date);
                                    psMovement.setString(4, "Abono inicial (migración)");
                                    psMovement.executeUpdate();
                                }
                            }
                        }
                    }
                    
                    // Eliminar tabla vieja
                    stmt.execute("DROP TABLE savings_old");
                    System.out.println("Database migration completed successfully.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during database migration: " + e.getMessage());
            e.printStackTrace();
        }

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
