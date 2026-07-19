package com.enterprise.utilities;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * JSON Utility class for serialization and deserialization using Jackson ObjectMapper.
 */
public class JsonUtil {

    private static final Logger log = LogManager.getLogger(JsonUtil.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        // Pretty print formatting enabled by default
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Deserialize JSON file to object.
     *
     * @param filePath Absolute or relative path to the JSON file
     * @param clazz    Target class
     * @return Deserialized object instance
     */
    public static <T> T deserialize(String filePath, Class<T> clazz) {
        log.info("Deserializing JSON file '{}' to class {}", filePath, clazz.getSimpleName());
        try {
            return mapper.readValue(new File(filePath), clazz);
        } catch (IOException e) {
            log.error("Failed to deserialize JSON file: {}", filePath, e);
            throw new RuntimeException("JSON deserialization failure.", e);
        }
    }

    /**
     * Deserialize JSON file to complex types (e.g. List<Map<String, Object>>).
     *
     * @param filePath      Absolute or relative path to the JSON file
     * @param typeReference Jackson TypeReference representing the generic type
     */
    public static <T> T deserialize(String filePath, TypeReference<T> typeReference) {
        log.info("Deserializing JSON file '{}' using TypeReference", filePath);
        try {
            return mapper.readValue(new File(filePath), typeReference);
        } catch (IOException e) {
            log.error("Failed to deserialize JSON file: {}", filePath, e);
            throw new RuntimeException("JSON deserialization failure.", e);
        }
    }

    /**
     * Deserialize JSON String to Object.
     */
    public static <T> T deserializeString(String jsonContent, Class<T> clazz) {
        try {
            return mapper.readValue(jsonContent, clazz);
        } catch (IOException e) {
            log.error("Failed to deserialize JSON string.", e);
            throw new RuntimeException("JSON string deserialization failure.", e);
        }
    }

    /**
     * Serialize Java object to JSON file.
     *
     * @param filePath Target file path
     * @param obj      Object to serialize
     */
    public static void serialize(String filePath, Object obj) {
        log.info("Serializing object of type {} to JSON file '{}'", obj.getClass().getSimpleName(), filePath);
        try {
            File file = new File(filePath);
            // Ensure parent directories exist
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            mapper.writeValue(file, obj);
            log.info("Serialization successful.");
        } catch (IOException e) {
            log.error("Failed to serialize object to file: {}", filePath, e);
            throw new RuntimeException("JSON serialization failure.", e);
        }
    }

    /**
     * Serialize Java object to JSON String.
     */
    public static String serializeToString(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (IOException e) {
            log.error("Failed to serialize object to string.", e);
            throw new RuntimeException("JSON string serialization failure.", e);
        }
    }
}
