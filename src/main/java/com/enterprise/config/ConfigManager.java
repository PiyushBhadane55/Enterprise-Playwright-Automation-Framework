package com.enterprise.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration Manager to load and read environment-specific properties dynamically.
 * Supports system property overrides for CI/CD integrations.
 */
public class ConfigManager {

    private static final Logger log = LogManager.getLogger(ConfigManager.class);
    private static final Properties properties = new Properties();

    static {
        loadProperties();
    }

    private static void loadProperties() {
        // Read environment from system property, default to 'qa'
        String env = System.getProperty("env");
        if (env == null || env.trim().isEmpty()) {
            env = "qa";
            log.info("System property 'env' not set. Defaulting to 'qa' environment.");
        } else {
            env = env.toLowerCase().trim();
            log.info("System property 'env' is set. Loading configuration for environment: {}", env);
        }

        String propFileName = "config/" + env + ".properties";
        try (InputStream inputStream = ConfigManager.class.getClassLoader().getResourceAsStream(propFileName)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Property file '" + propFileName + "' not found in classpath.");
            }
            properties.load(inputStream);
            log.info("Successfully loaded configuration properties from: {}", propFileName);
        } catch (Exception e) {
            log.error("Failed to load environment configuration file: {}", propFileName, e);
            throw new RuntimeException("Initialization of ConfigManager failed.", e);
        }
    }

    /**
     * Get property value by key. System properties override properties file values.
     *
     * @param key the property key
     * @return the property value, or null if not found
     */
    public static String get(String key) {
        // System property overrides file property
        String systemProp = System.getProperty(key);
        if (systemProp != null) {
            log.debug("System property override: {} = {}", key, systemProp);
            return systemProp;
        }

        String fileProp = properties.getProperty(key);
        if (fileProp == null) {
            log.warn("Property key '{}' not found in configurations.", key);
            return null;
        }
        return fileProp.trim();
    }

    /**
     * Get property value by key, or return the default value if key is not found.
     *
     * @param key          the property key
     * @param defaultValue default value to return if key doesn't exist
     * @return property value or defaultValue
     */
    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Get property value as Integer.
     *
     * @param key the property key
     * @return Integer value, or null if not found or invalid
     */
    public static Integer getInt(String key) {
        String val = get(key);
        if (val == null) return null;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            log.error("Value for property '{}' is not a valid integer: {}", key, val);
            return null;
        }
    }

    /**
     * Get property value as Integer, or return default.
     */
    public static int getInt(String key, int defaultValue) {
        Integer val = getInt(key);
        return val != null ? val : defaultValue;
    }

    /**
     * Get property value as Boolean.
     *
     * @param key the property key
     * @return boolean value (defaults to false if key not found)
     */
    public static boolean getBoolean(String key) {
        String val = get(key);
        return val != null && Boolean.parseBoolean(val);
    }
}
