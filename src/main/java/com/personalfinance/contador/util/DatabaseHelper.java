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
        // Cargar el driver JDBC de SQLite de manera explícita
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Error al cargar el driver de SQLite JDBC: " + e.getMessage());
        }
    }

    /**
     * Retorna una nueva conexión a la base de datos SQLite.
     * La base de datos se guarda en la raíz del proyecto (directorio de ejecución).
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(CONNECTION_URL);
    }

    /**
     * Inicializa la base de datos ejecutando el script init.sql si es necesario.
     */
    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Cargar el script init.sql desde los recursos
            InputStream is = DatabaseHelper.class.getResourceAsStream("/db/init.sql");
            if (is == null) {
                throw new RuntimeException("No se encontró el script de inicialización init.sql en los recursos.");
            }

            String scriptContent;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
                scriptContent = reader.lines().collect(Collectors.joining("\n"));
            }

            // Separar el script en sentencias individuales por punto y coma (;)
            // Esto evita problemas de ejecución de múltiples sentencias en una sola llamada en SQLite JDBC
            String[] sqlStatements = scriptContent.split(";");
            for (String sql : sqlStatements) {
                String cleanSql = sql.trim();
                if (!cleanSql.isEmpty()) {
                    stmt.execute(cleanSql);
                }
            }
            System.out.println("Base de datos inicializada correctamente (Tablas y e Índices validados).");
        } catch (Exception e) {
            System.err.println("Error al inicializar la base de datos SQLite: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
