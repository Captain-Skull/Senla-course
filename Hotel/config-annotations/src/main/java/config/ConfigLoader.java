package config;

import annotations.ConfigClass;
import annotations.ConfigProperty;
import annotations.PropertyType;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class ConfigLoader {

    public static void loadConfig(Object configObject) {
        Class<?> clazz = configObject.getClass();

        String configFileName = "hotel.properties";
        if (clazz.isAnnotationPresent(ConfigClass.class)) {
            ConfigClass classAnnotation = clazz.getAnnotation(ConfigClass.class);
            configFileName = classAnnotation.configFileName();
        }

        Properties properties = loadProperties(configFileName);

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(ConfigProperty.class)) {
                processField(configObject, field, properties);
            }
        }
    }

    private static Properties loadProperties(String fileName) {
        Properties properties = new Properties();

        try (InputStream input = new FileInputStream(fileName)) {
            properties.load(input);
        } catch (IOException e) {
            try (InputStream input = ConfigLoader.class.getClassLoader()
                    .getResourceAsStream(fileName)) {
                if (input != null) {
                    properties.load(input);
                } else {
                    System.err.println("Config file not found: " + fileName);
                }
            } catch (IOException ex) {
                System.err.println("Cannot load config file: " + fileName);
            }
        }

        return properties;
    }

    private static void processField(Object object, Field field, Properties properties) {
        ConfigProperty annotation = field.getAnnotation(ConfigProperty.class);

        String propertyKey = annotation.propertyName().isEmpty() ?
                field.getDeclaringClass().getSimpleName().toLowerCase() + "." + field.getName().toLowerCase() :
                annotation.propertyName();

        String propertyValue = properties.getProperty(propertyKey);
        if (propertyValue == null) {
            return;
        }

        try {
            Object value = convertValue(propertyValue, field.getType(), annotation.type());
            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception e) {
            System.err.println("Error setting field " + field.getName() + ": " + e.getMessage());
        }
    }

    private static Object convertValue(String value, Class<?> targetType, PropertyType propertyType) {
        value = value.trim();
        PropertyType actualType = (propertyType == PropertyType.AUTO) ?
                determineType(targetType) : propertyType;

        try {
            switch (actualType) {
                case STRING:
                    return value;
                case INTEGER:
                    return Integer.parseInt(value);
                case LONG:
                    return Long.parseLong(value);
                case DOUBLE:
                    return Double.parseDouble(value);
                case BOOLEAN:
                    return Boolean.parseBoolean(value);
                case ARRAY:
                    return convertToArray(value, targetType);
                case LIST:
                    return Arrays.asList(value.split(","));
                default:
                    return value;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot convert value '" + value + "' to type " + actualType, e);
        }
    }

    private static PropertyType determineType(Class<?> type) {
        if (type == String.class) return PropertyType.STRING;
        if (type == int.class || type == Integer.class) return PropertyType.INTEGER;
        if (type == long.class || type == Long.class) return PropertyType.LONG;
        if (type == double.class || type == Double.class) return PropertyType.DOUBLE;
        if (type == boolean.class || type == Boolean.class) return PropertyType.BOOLEAN;
        if (type.isArray()) return PropertyType.ARRAY;
        if (List.class.isAssignableFrom(type)) return PropertyType.LIST;
        return PropertyType.STRING;
    }

    private static Object convertToArray(String value, Class<?> targetType) {
        String[] parts = value.split(",");

        if (targetType == String[].class) {
            return parts;
        } else if (targetType == int[].class) {
            int[] array = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                array[i] = Integer.parseInt(parts[i].trim());
            }
            return array;
        } else if (targetType == double[].class) {
            double[] array = new double[parts.length];
            for (int i = 0; i < parts.length; i++) {
                array[i] = Double.parseDouble(parts[i].trim());
            }
            return array;
        }

        return parts;
    }
}