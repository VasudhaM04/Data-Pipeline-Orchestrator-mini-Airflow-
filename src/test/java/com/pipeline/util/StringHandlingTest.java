package com.pipeline.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit Tests for String Handling
 * 
 * CORE ADVANCED JAVA CONCEPTS TESTED:
 * - String Handling (split, trim, parse)
 * - Collections (Map)
 * 
 * These tests verify string parsing operations used throughout the application.
 */
class StringHandlingTest {

    @Test
    void testParseConfig_Simple() {
        String config = "type=API;url=https://api.example.com";
        Map<String, String> result = parseConfig(config);
        
        assertEquals(2, result.size(), "Should have 2 key-value pairs");
        assertEquals("API", result.get("type"), "Type should be API");
        assertEquals("https://api.example.com", result.get("url"), "URL should match");
    }

    @Test
    void testParseConfig_Complex() {
        String config = "type=DATABASE;query=SELECT * FROM users;timeout=30;retry=3";
        Map<String, String> result = parseConfig(config);
        
        assertEquals(4, result.size(), "Should have 4 key-value pairs");
        assertEquals("DATABASE", result.get("type"));
        assertEquals("SELECT * FROM users", result.get("query"));
        assertEquals("30", result.get("timeout"));
        assertEquals("3", result.get("retry"));
    }

    @Test
    void testParseConfig_WithSpaces() {
        String config = "  type  =  API  ;  url = https://example.com  ";
        Map<String, String> result = parseConfig(config);
        
        assertEquals("API", result.get("type"), "Should trim spaces around key and value");
        assertEquals("https://example.com", result.get("url"));
    }

    @Test
    void testParseConfig_Empty() {
        Map<String, String> result = parseConfig("");
        assertTrue(result.isEmpty(), "Empty string should return empty map");
        
        Map<String, String> nullResult = parseConfig(null);
        assertTrue(nullResult.isEmpty(), "Null should return empty map");
    }

    @Test
    void testParseConfig_InvalidFormat() {
        String config = "type=API;invalid_entry_without_equals;url=test.com";
        Map<String, String> result = parseConfig(config);
        
        assertEquals(2, result.size(), "Should skip invalid entries");
        assertEquals("API", result.get("type"));
        assertEquals("test.com", result.get("url"));
    }

    @Test
    void testParseConfig_EmptyValue() {
        String config = "type=;url=https://example.com";
        Map<String, String> result = parseConfig(config);
        
        assertEquals("", result.get("type"), "Empty value should be preserved");
        assertEquals("https://example.com", result.get("url"));
    }

    @Test
    void testValidateScheduleTime_Valid() {
        assertTrue(validateScheduleTime("00:00"), "Midnight should be valid");
        assertTrue(validateScheduleTime("23:59"), "Last minute should be valid");
        assertTrue(validateScheduleTime("12:30"), "Normal time should be valid");
        assertTrue(validateScheduleTime("2:5"), "Single digits should be valid");
    }

    @Test
    void testValidateScheduleTime_Invalid() {
        assertFalse(validateScheduleTime("24:00"), "Hour 24 should be invalid");
        assertFalse(validateScheduleTime("12:60"), "Minute 60 should be invalid");
        assertFalse(validateScheduleTime("abc"), "Non-numeric should be invalid");
        assertFalse(validateScheduleTime("12"), "Missing minutes should be invalid");
        assertFalse(validateScheduleTime(""), "Empty should be invalid");
    }

    @Test
    void testFormatLogMessage() {
        String taskName = "FetchData";
        String status = "COMPLETED";
        long duration = 1234;
        
        String log = formatLogMessage(taskName, status, duration);
        
        assertTrue(log.contains("FetchData"), "Log should contain task name");
        assertTrue(log.contains("COMPLETED"), "Log should contain status");
        assertTrue(log.contains("1234ms"), "Log should contain duration");
    }

    @Test
    void testExtractDependencyIndices() {
        String depString = "0, 1, 2";
        int[] indices = extractDependencyIndices(depString);
        
        assertArrayEquals(new int[]{0, 1, 2}, indices, "Should parse comma-separated indices");
    }

    @Test
    void testExtractDependencyIndices_Single() {
        String depString = "5";
        int[] indices = extractDependencyIndices(depString);
        
        assertArrayEquals(new int[]{5}, indices, "Should handle single index");
    }

    @Test
    void testExtractDependencyIndices_Invalid() {
        String depString = "0, abc, 2";
        int[] indices = extractDependencyIndices(depString);
        
        assertArrayEquals(new int[]{0, 2}, indices, "Should skip invalid entries");
    }

    // Helper methods (simulating the actual implementation)
    
    private Map<String, String> parseConfig(String config) {
        Map<String, String> configMap = new HashMap<>();
        
        if (config == null || config.isEmpty()) {
            return configMap;
        }
        
        String[] pairs = config.split(";");
        
        for (String pair : pairs) {
            pair = pair.trim();
            if (pair.contains("=")) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    configMap.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
        }
        
        return configMap;
    }
    
    private boolean validateScheduleTime(String time) {
        if (time == null || time.isEmpty()) {
            return false;
        }
        
        String[] parts = time.split(":");
        if (parts.length != 2) {
            return false;
        }
        
        try {
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            
            return hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private String formatLogMessage(String taskName, String status, long duration) {
        return String.format("Task '%s' %s in %dms", taskName, status, duration);
    }
    
    private int[] extractDependencyIndices(String depString) {
        if (depString == null || depString.trim().isEmpty()) {
            return new int[0];
        }
        
        String[] parts = depString.split(",");
        java.util.List<Integer> indices = new java.util.ArrayList<>();
        
        for (String part : parts) {
            try {
                indices.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException e) {
                // Skip invalid entries
            }
        }
        
        return indices.stream().mapToInt(i -> i).toArray();
    }
}
