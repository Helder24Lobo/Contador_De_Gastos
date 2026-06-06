package com.personalfinance.contador.service;

import com.personalfinance.contador.util.DatabaseHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;

public class BackupService {

    private static final String DB_FILE_PATH = "contador_gastos.db";
    private static final String BACKUP_DIR_NAME = "backups";
    private static final int MAX_AUTO_BACKUPS = 5;

    /**
     * Realiza una copia de seguridad automática de la base de datos.
     * Mantiene como máximo MAX_AUTO_BACKUPS archivos en la carpeta 'backups'.
     */
    public static void performAutoBackup() {
        File dbFile = new File(DB_FILE_PATH);
        if (!dbFile.exists()) {
            return; // No hay base de datos que respaldar todavía
        }

        File backupDir = new File(BACKUP_DIR_NAME);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File backupFile = new File(backupDir, "auto_backup_contador_" + timestamp + ".db");

        try {
            // Copiar archivo de base de datos
            Files.copy(dbFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Copia de seguridad automática creada: " + backupFile.getAbsolutePath());

            // Rotar backups antiguos
            rotateOldBackups(backupDir);
        } catch (IOException e) {
            System.err.println("Error al realizar la copia de seguridad automática: " + e.getMessage());
        }
    }

    /**
     * Exporta manualmente la base de datos a un archivo de destino seleccionado por el usuario.
     */
    public static void exportDatabase(File destFile) throws IOException {
        File dbFile = new File(DB_FILE_PATH);
        if (!dbFile.exists()) {
            throw new IOException("La base de datos actual no existe para ser exportada.");
        }
        Files.copy(dbFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Importa manualmente una base de datos externa reemplazando la actual.
     * Cierra cualquier conexión abierta antes de reemplazar el archivo.
     */
    public static void importDatabase(File srcFile) throws IOException {
        if (!srcFile.exists()) {
            throw new IOException("El archivo de origen para la importación no existe.");
        }

        // Forzar recolección de basura para liberar el descriptor del archivo si SQLite JDBC aún lo tiene retenido
        System.gc();

        File dbFile = new File(DB_FILE_PATH);
        Files.copy(srcFile.toPath(), dbFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        
        // Re-inicializar para asegurarse de que las tablas estén al día y el archivo sea válido
        DatabaseHelper.initializeDatabase();
    }

    private static void rotateOldBackups(File backupDir) {
        File[] files = backupDir.listFiles((dir, name) -> name.startsWith("auto_backup_contador_") && name.endsWith(".db"));
        if (files != null && files.length > MAX_AUTO_BACKUPS) {
            // Ordenar por fecha de modificación (los más antiguos primero)
            Arrays.sort(files, Comparator.comparingLong(File::lastModified));
            int toDelete = files.length - MAX_AUTO_BACKUPS;
            for (int i = 0; i < toDelete; i++) {
                if (files[i].delete()) {
                    System.out.println("Backup antiguo rotado (eliminado): " + files[i].getName());
                }
            }
        }
    }
}
