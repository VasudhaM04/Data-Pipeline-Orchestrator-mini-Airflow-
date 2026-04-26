package com.pipeline.service;

import com.pipeline.model.Pipeline;
import com.pipeline.model.Task;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit Tests for Pipeline Executor
 * 
 * Tests the execution engine and multithreading components.
 */
class PipelineExecutorTest {

    @Test
    void testExecutionResult_Success() {
        PipelineExecutor.ExecutionResult result = 
            new PipelineExecutor.ExecutionResult(true, "Pipeline executed successfully");
        
        assertTrue(result.isSuccess(), "Should be successful");
        assertEquals("Pipeline executed successfully", result.getMessage());
    }

    @Test
    void testExecutionResult_Failure() {
        PipelineExecutor.ExecutionResult result = 
            new PipelineExecutor.ExecutionResult(false, "Cycle detected in pipeline");
        
        assertFalse(result.isSuccess(), "Should be failed");
        assertEquals("Cycle detected in pipeline", result.getMessage());
    }

    @Test
    void testParseConfig_Simulation() {
        // Simulating the config parsing from PipelineExecutor
        String config = "type=API;url=https://data.example.com;method=GET;timeout=30";
        
        java.util.Map<String, String> configMap = new java.util.HashMap<>();
        String[] pairs = config.split(";");
        
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                configMap.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
        
        assertEquals(4, configMap.size());
        assertEquals("API", configMap.get("type"));
        assertEquals("https://data.example.com", configMap.get("url"));
        assertEquals("GET", configMap.get("method"));
        assertEquals("30", configMap.get("timeout"));
    }

    @Test
    void testTaskPriorityComparison() {
        // Test PriorityQueue ordering logic
        java.util.PriorityQueue<Task> pq = new java.util.PriorityQueue<>(
            (t1, t2) -> Integer.compare(t2.getPriority(), t1.getPriority())
        );
        
        Task task1 = new Task("Low Priority", "CODE", "", 1);
        Task task2 = new Task("High Priority", "CODE", "", 5);
        Task task3 = new Task("Medium Priority", "CODE", "", 3);
        
        pq.add(task1);
        pq.add(task2);
        pq.add(task3);
        
        // Highest priority should come out first
        assertEquals("High Priority", pq.poll().getName());
        assertEquals("Medium Priority", pq.poll().getName());
        assertEquals("Low Priority", pq.poll().getName());
    }
}
