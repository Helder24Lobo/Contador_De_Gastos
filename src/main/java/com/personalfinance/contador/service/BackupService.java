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
     * Performs an automatic database backup.
     * Keeps at most MAX_AUTO_BACKUPS files in the 'backups' folder.
     */
    public static void performAutoBackup() {
        File dbFile = new File(DB_FILE_PATH);
        if (!dbFile.exists()) {
            return; // No database to back up yet
        }

        File backupDir = new File(BACKUP_DIR_NAME);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File backupFile = new File(backupDir, "auto_backup_contador_" + timestamp + ".db");

        try {
            // Copy database file
            Files.copy(dbFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Automatic backup created: " + backupFile.getAbsolutePath());

            // Rotate old backups
            rotateOldBackups(backupDir);
        } catch (IOException e) {
            System.err.println("Error performing automatic backup: " + e.getMessage());
        }
    }

    /**
     * Manually exports the database to a destination file selected by the user.
     */
    public static void exportDatabase(File destFile) throws IOException {
        File dbFile = new File(DB_FILE_PATH);
        if (!dbFile.exists()) {
            throw new IOException("The current database does not exist for export.");
        }
        Files.copy(dbFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Manually imports an external database replacing the current one.
     * Closes any open connection before replacing the file.
     */
    public static void importDatabase(File srcFile) throws IOException {
        if (!srcFile.exists()) {
            throw new IOException("The source file for import does not exist.");
        }

        // Force garbage collection to release the file descriptor if SQLite JDBC still holds it
        System.gc();

        File dbFile = new File(DB_FILE_PATH);
        Files.copy(srcFile.toPath(), dbFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Re-initialize to ensure tables are up to date and the file is valid
        DatabaseHelper.initializeDatabase();
    }

    private static void rotateOldBackups(File backupDir) {
        File[] files = backupDir.listFiles((dir, name) -> name.startsWith("auto_backup_contador_") && name.endsWith(".db"));
        if (files != null && files.length > MAX_AUTO_BACKUPS) {
            // Sort by last modified date (oldest first)
            Arrays.sort(files, Comparator.comparingLong(File::lastModified));
            int toDelete = files.length - MAX_AUTO_BACKUPS;
            for (int i = 0; i < toDelete; i++) {
                if (files[i].delete()) {
                    System.out.println("Old backup rotated (deleted): " + files[i].getName());
                }
            }
        }
    }
}
