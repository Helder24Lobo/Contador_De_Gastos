package com.personalfinance.contador.util;

import java.io.*;
import java.util.Properties;

public class ConfigManager {

    private static final String CONFIG_FILE_PATH = "config.properties";
    private static final Properties properties = new Properties();

    static {
        loadConfig();
    }

    public static void loadConfig() {
        File configFile = new File(CONFIG_FILE_PATH);
        if (configFile.exists()) {
            try (InputStream input = new FileInputStream(configFile)) {
                properties.load(input);
            } catch (IOException e) {
                System.err.println("Error loading config.properties: " + e.getMessage());
            }
        } else {
            // Default values
            properties.setProperty("theme", "dark");
            saveConfig();
        }
    }

    public static void saveConfig() {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE_PATH)) {
            properties.store(output, "Personal Finance Manager Settings");
        } catch (IOException e) {
            System.err.println("Error saving config.properties: " + e.getMessage());
        }
    }

    public static String getTheme() {
        return properties.getProperty("theme", "dark");
    }

    public static void setTheme(String theme) {
        properties.setProperty("theme", theme);
        saveConfig();
    }
}
