package com.pipeline.service;

import com.pipeline.dao.*;
import com.pipeline.model.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * Pipeline Executor Service
 * 
 * CORE ADVANCED JAVA CONCEPTS:
 * - Multithreading: ExecutorService, CompletableFuture
 * - Collections: HashMap, Set, Queue
 * - String Handling: Log formatting, timestamp
 * 
 * Executes pipeline tasks respecting DAG dependencies using
 * multi-threaded execution where possible.
 */
public class PipelineExecutor {

    // Thread pool for task execution
    private ExecutorService executorService;
    
    // DAOs for persistence
    private PipelineDao pipelineDao;
    private TaskDao taskDao;
    private ExecutionLogDao executionLogDao;
    
    // Date formatter for timestamps
    private DateTimeFormatter formatter;

    /**
     * Constructor - Initialize with DAOs
     */
    public PipelineExecutor() {
        this.pipelineDao = new PipelineDao();
        this.taskDao = new TaskDao();
        this.executionLogDao = new ExecutionLogDao();
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        // Create thread pool with fixed size
        this.executorService = Executors.newFixedThreadPool(4);
    }

    /**
     * Execute a pipeline synchronously
     * Main execution method that orchestrates the entire pipeline
     * 
     * @param pipelineId Pipeline ID to execute
     * @return Execution result with status
     */
    public ExecutionResult executePipeline(Integer pipelineId) {
        // Load pipeline with all tasks and dependencies
        Pipeline pipeline = pipelineDao.findByIdWithTasks(pipelineId);
        
        if (pipeline == null) {
            return new ExecutionResult(false, "Pipeline not found: " + pipelineId);
        }
        
        // Update pipeline status to RUNNING
        updatePipelineStatus(pipeline, "RUNNING");
        
        // Reset all task statuses
        taskDao.resetTaskStatuses(pipelineId);
        
        // Initialize DAG Engine
        DagEngine dagEngine = new DagEngine(pipeline);
        
        // Check for cycles
        if (dagEngine.hasCycle()) {
            updatePipelineStatus(pipeline, "FAILED");
            return new ExecutionResult(false, "Cycle detected in pipeline dependencies");
        }
        
        try {
            // Get all tasks
            Set<Task> allTasks = new HashSet<>(pipeline.getTasks());
            
            // Track execution status
            Map<Integer, Future<TaskResult>> runningTasks = new HashMap<>();
            Set<Integer> failedTasks = new HashSet<>();
            
            // Initial ready tasks (no dependencies)
            Queue<Task> readyQueue = new LinkedList<>(dagEngine.getInitialReadyTasks(allTasks));
            
            // Process tasks until all done
            while (!readyQueue.isEmpty() || !runningTasks.isEmpty()) {
                
                // Submit ready tasks to thread pool
                while (!readyQueue.isEmpty() && runningTasks.size() < 4) {
                    Task task = readyQueue.poll();
                    
                    // Update task status
                    task.setStatus("RUNNING");
                    taskDao.updateStatus(task.getId(), "RUNNING");
                    
                    // Create execution log entry
                    ExecutionLog log = createExecutionLog(task, "RUNNING", "Task started");
                    
                    // Submit task for execution
                    Future<TaskResult> future = executorService.submit(() -> executeTask(task, log));
                    runningTasks.put(task.getId(), future);
                }
                
                // Check completed tasks
                Iterator<Map.Entry<Integer, Future<TaskResult>>> iterator = runningTasks.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Integer, Future<TaskResult>> entry = iterator.next();
                    Future<TaskResult> future = entry.getValue();
                    
                    if (future.isDone()) {
                        iterator.remove();
                        
                        try {
                            TaskResult result = future.get();
                            Task completedTask = result.getTask();
                            
                            if (result.isSuccess()) {
                                // Mark as completed in DAG engine
                                dagEngine.markCompleted(completedTask.getId());
                                completedTask.setStatus("COMPLETED");
                                taskDao.updateStatus(completedTask.getId(), "COMPLETED");
                                
                                // Update execution log
                                updateExecutionLog(result.getLog(), "COMPLETED", "Task completed successfully");
                                
                                // Find newly ready tasks
                                Set<Task> newlyReady = dagEngine.getNewlyReadyTasks(completedTask.getId(), allTasks);
                                readyQueue.addAll(newlyReady);
                                
                            } else {
                                // Task failed
                                failedTasks.add(completedTask.getId());
                                completedTask.setStatus("FAILED");
                                taskDao.updateStatus(completedTask.getId(), "FAILED");
                                updateExecutionLog(result.getLog(), "FAILED", result.getErrorMessage());
                                
                                // Fail dependent tasks
                                failDependentTasks(completedTask, allTasks, dagEngine);
                            }
                            
                        } catch (InterruptedException | ExecutionException e) {
                            Task failedTask = findTaskById(allTasks, entry.getKey());
                            if (failedTask != null) {
                                failedTask.setStatus("FAILED");
                                taskDao.updateStatus(failedTask.getId(), "FAILED");
                                failedTasks.add(failedTask.getId());
                            }
                        }
                    }
                }
                
                // Small delay to prevent CPU spinning
                if (runningTasks.size() >= 4) {
                    Thread.sleep(100);
                }
            }
            
            // Determine final status
            if (failedTasks.isEmpty()) {
                updatePipelineStatus(pipeline, "SUCCESS");
                return new ExecutionResult(true, "Pipeline executed successfully");
            } else {
                updatePipelineStatus(pipeline, "PARTIAL_FAILURE");
                return new ExecutionResult(false, "Pipeline completed with " + failedTasks.size() + " failed tasks");
            }
            
        } catch (Exception e) {
            updatePipelineStatus(pipeline, "FAILED");
            return new ExecutionResult(false, "Pipeline execution error: " + e.getMessage());
        }
    }

    /**
     * Execute a single task
     * Simulates task execution based on task type
     * 
     * @param task Task to execute
     * @param log Execution log entry
     * @return TaskResult with execution outcome
     */
    private TaskResult executeTask(Task task, ExecutionLog log) {
        long startTime = System.currentTimeMillis();
        
        try {
            // String handling: Parse task configuration
            String config = task.getConfig();
            Map<String, String> configMap = parseConfig(config);
            
            // Simulate task execution based on type
            String taskType = task.getType();
            
            switch (taskType.toUpperCase()) {
                case "API":
                    executeApiTask(task, configMap);
                    break;
                case "DATABASE":
                case "DB":
                    executeDatabaseTask(task, configMap);
                    break;
                case "CODE":
                    executeCodeTask(task, configMap);
                    break;
                case "TRANSFORM":
                    executeTransformTask(task, configMap);
                    break;
                case "LOAD":
                    executeLoadTask(task, configMap);
                    break;
                default:
                    // Generic task execution
                    Thread.sleep(1000); // Simulate work
            }
            
            long duration = System.currentTimeMillis() - startTime;
            
            // Update log
            log.setEndTime(getCurrentTimestamp());
            log.setDurationMs(duration);
            executionLogDao.update(log);
            
            return new TaskResult(task, true, log, null);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            
            log.setEndTime(getCurrentTimestamp());
            log.setDurationMs(duration);
            executionLogDao.update(log);
            
            return new TaskResult(task, false, log, e.getMessage());
        }
    }

    /**
     * Parse task configuration string into Map
     * Module: String Handling + Collections
     * 
     * Example config: "type=API;url=https://api.example.com;method=GET"
     * 
     * @param config Configuration string
     * @return Map of key-value pairs
     */
    private Map<String, String> parseConfig(String config) {
        Map<String, String> configMap = new HashMap<>();
        
        if (config == null || config.isEmpty()) {
            return configMap;
        }
        
        // String handling: split by delimiter
        String[] pairs = config.split(";");
        
        for (String pair : pairs) {
            // String handling: trim and split by =
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

    /**
     * Execute API task simulation
     */
    private void executeApiTask(Task task, Map<String, String> config) throws InterruptedException {
        String url = config.getOrDefault("url", "https://api.example.com");
        String method = config.getOrDefault("method", "GET");
        
        // Simulate API call
        Thread.sleep(500 + (int)(Math.random() * 1000));
        
        // Simulate occasional failures (5% chance)
        if (Math.random() < 0.05) {
            throw new RuntimeException("API call failed: HTTP 500");
        }
    }

    /**
     * Execute Database task simulation
     */
    private void executeDatabaseTask(Task task, Map<String, String> config) throws InterruptedException {
        String query = config.getOrDefault("query", "SELECT * FROM table");
        
        // Simulate database operation
        Thread.sleep(300 + (int)(Math.random() * 800));
    }

    /**
     * Execute Code task simulation
     */
    private void executeCodeTask(Task task, Map<String, String> config) throws InterruptedException {
        String script = config.getOrDefault("script", "default.py");
        
        // Simulate script execution
        Thread.sleep(800 + (int)(Math.random() * 1200));
    }

    /**
     * Execute Transform task simulation
     */
    private void executeTransformTask(Task task, Map<String, String> config) throws InterruptedException {
        // Simulate data transformation
        Thread.sleep(400 + (int)(Math.random() * 600));
    }

    /**
     * Execute Load task simulation
     */
    private void executeLoadTask(Task task, Map<String, String> config) throws InterruptedException {
        // Simulate data loading
        Thread.sleep(600 + (int)(Math.random() * 1000));
    }

    /**
     * Create execution log entry
     */
    private ExecutionLog createExecutionLog(Task task, String status, String message) {
        ExecutionLog log = new ExecutionLog();
        log.setTask(task);
        log.setStatus(status);
        log.setMessage(message);
        log.setStartTime(getCurrentTimestamp());
        
        return executionLogDao.create(log);
    }

    /**
     * Update execution log
     */
    private void updateExecutionLog(ExecutionLog log, String status, String message) {
        log.setStatus(status);
        log.setMessage(message);
        log.setEndTime(getCurrentTimestamp());
        executionLogDao.update(log);
    }

    /**
     * Mark dependent tasks as failed
     */
    private void failDependentTasks(Task failedTask, Set<Task> allTasks, DagEngine dagEngine) {
        List<Integer> dependents = dagEngine.getDependents(failedTask.getId());
        
        for (Integer dependentId : dependents) {
            Task dependent = findTaskById(allTasks, dependentId);
            if (dependent != null && !dependent.getStatus().equals("FAILED")) {
                dependent.setStatus("FAILED");
                taskDao.updateStatus(dependent.getId(), "FAILED");
                
                // Create failure log
                ExecutionLog log = createExecutionLog(dependent, "FAILED", 
                    "Failed because dependency '" + failedTask.getName() + "' failed");
                updateExecutionLog(log, "FAILED", "Dependency failure cascade");
                
                // Recursively fail dependents
                failDependentTasks(dependent, allTasks, dagEngine);
            }
        }
    }

    /**
     * Update pipeline status in database
     */
    private void updatePipelineStatus(Pipeline pipeline, String status) {
        pipeline.setStatus(status);
        if (status.equals("SUCCESS") || status.equals("PARTIAL_FAILURE") || status.equals("FAILED")) {
            pipeline.setLastRun(getCurrentTimestamp());
        }
        pipelineDao.updateStatus(pipeline.getId(), status, pipeline.getLastRun());
    }

    /**
     * Get current timestamp as formatted string
     * @return Formatted timestamp
     */
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(formatter);
    }

    /**
     * Find task by ID from set
     */
    private Task findTaskById(Set<Task> tasks, Integer taskId) {
        for (Task task : tasks) {
            if (task.getId().equals(taskId)) {
                return task;
            }
        }
        return null;
    }

    /**
     * Shutdown the executor service
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
    }

    /**
     * Execution Result inner class
     */
    public static class ExecutionResult {
        private final boolean success;
        private final String message;

        public ExecutionResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Task Result inner class
     */
    private static class TaskResult {
        private final Task task;
        private final boolean success;
        private final ExecutionLog log;
        private final String errorMessage;

        public TaskResult(Task task, boolean success, ExecutionLog log, String errorMessage) {
            this.task = task;
            this.success = success;
            this.log = log;
            this.errorMessage = errorMessage;
        }

        public Task getTask() {
            return task;
        }

        public boolean isSuccess() {
            return success;
        }

        public ExecutionLog getLog() {
            return log;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
