package com.itsazni.kasir.hm.utils;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class AppConfig {
    
    private static final String CONFIG_FILE = "config.properties";
    private static AppConfig instance;
    private Properties properties;
    
    private static final String DEFAULT_DB_HOST = "localhost";
    private static final String DEFAULT_DB_PORT = "3306";
    private static final String DEFAULT_DB_NAME = "kasir_hm";
    private static final String DEFAULT_DB_USER = "root";
    private static final String DEFAULT_DB_PASSWORD = "";
    
    private AppConfig() {
        properties = new Properties();
        loadConfig();
    }
    
    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }
    
    private void loadConfig() {
        Path configPath = getConfigPath();
        
        if (Files.exists(configPath)) {
            try (InputStream is = Files.newInputStream(configPath)) {
                properties.load(is);
            } catch (IOException e) {
                System.err.println("Gagal membaca config: " + e.getMessage());
                setDefaults();
            }
        } else {
            setDefaults();
            saveConfig();
        }
    }
    
    private void setDefaults() {
        properties.setProperty("db.host", DEFAULT_DB_HOST);
        properties.setProperty("db.port", DEFAULT_DB_PORT);
        properties.setProperty("db.name", DEFAULT_DB_NAME);
        properties.setProperty("db.user", DEFAULT_DB_USER);
        properties.setProperty("db.password", DEFAULT_DB_PASSWORD);
        properties.setProperty("app.name", "Kasir HM");
        properties.setProperty("app.version", "1.0");
    }
    
    public void saveConfig() {
        Path configPath = getConfigPath();
        try (OutputStream os = Files.newOutputStream(configPath)) {
            properties.store(os, "Kasir HM Configuration");
        } catch (IOException e) {
            System.err.println("Gagal menyimpan config: " + e.getMessage());
        }
    }
    
    private Path getConfigPath() {
        try {
            String jarDir = new File(AppConfig.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).getParent();
            return Paths.get(jarDir, CONFIG_FILE);
        } catch (Exception e) {
            return Paths.get(CONFIG_FILE);
        }
    }
    
    public String getDbHost() {
        return properties.getProperty("db.host", DEFAULT_DB_HOST);
    }
    
    public String getDbPort() {
        return properties.getProperty("db.port", DEFAULT_DB_PORT);
    }
    
    public String getDbName() {
        return properties.getProperty("db.name", DEFAULT_DB_NAME);
    }
    
    public String getDbUser() {
        return properties.getProperty("db.user", DEFAULT_DB_USER);
    }
    
    public String getDbPassword() {
        return properties.getProperty("db.password", DEFAULT_DB_PASSWORD);
    }
    
    public String getDbUrl() {
        return String.format("jdbc:mysql://%s:%s/%s", getDbHost(), getDbPort(), getDbName());
    }
    
    public String getAppName() {
        return properties.getProperty("app.name", "Kasir HM");
    }
    
    public String getAppVersion() {
        return properties.getProperty("app.version", "1.0");
    }
    
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
    
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
