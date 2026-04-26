package com.pipeline.service;

import com.pipeline.dao.PipelineDao;
import com.pipeline.model.Pipeline;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Pipeline Scheduler Service
 * 
 * CORE ADVANCED JAVA CONCEPTS:
 * - Multithreading: ScheduledExecutorService for periodic task scheduling
 * - String Handling: Time parsing and formatting
 * - Collections: List iteration
 * 
 * Automatically executes pipelines based on their schedule configuration.
 */
public class PipelineScheduler {

    // Singleton instance
    private static PipelineScheduler instance;
    
    // Scheduled executor for periodic checks
    private ScheduledExecutorService scheduler;
    
    // Pipeline executor for running pipelines
    private PipelineExecutor pipelineExecutor;
    
    // Pipeline DAO for loading scheduled pipelines
    private PipelineDao pipelineDao;
    
    // DateTime formatter for schedule parsing
    private DateTimeFormatter timeFormatter;
    
    // Currently scheduled task
    private ScheduledFuture<?> scheduledTask;
    
    // Scheduler running flag
    private boolean isRunning;

    /**
     * Private constructor - Singleton pattern
     */
    private PipelineScheduler() {
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.pipelineExecutor = new PipelineExecutor();
        this.pipelineDao = new PipelineDao();
        this.timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        this.isRunning = false;
    }

    /**
     * Get singleton instance
     * @return PipelineScheduler instance
     */
    public static synchronized PipelineScheduler getInstance() {
        if (instance == null) {
            instance = new PipelineScheduler();
        }
        return instance;
    }

    /**
     * Start the scheduler
     * Checks every minute for pipelines that need to be executed
     */
    public synchronized void start() {
        if (isRunning) {
            System.out.println("[Scheduler] Already running");
            return;
        }
        
        isRunning = true;
        System.out.println("[Scheduler] Starting...");
        
        // Schedule periodic check every 1 minute
        scheduledTask = scheduler.scheduleAtFixedRate(
            this::checkAndExecuteScheduledPipelines,
            0,  // Initial delay
            1,  // Period
            TimeUnit.MINUTES
        );
        
        System.out.println("[Scheduler] Started - checking every 1 minute");
    }

    /**
     * Stop the scheduler
     */
    public synchronized void stop() {
        if (!isRunning) {
            return;
        }
        
        isRunning = false;
        System.out.println("[Scheduler] Stopping...");
        
        // Cancel scheduled task
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
        }
        
        // Shutdown executor
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
        
        // Shutdown pipeline executor
        pipelineExecutor.shutdown();
        
        System.out.println("[Scheduler] Stopped");
    }

    /**
     * Check for scheduled pipelines and execute them
     * This runs periodically based on the scheduler configuration
     */
    private void checkAndExecuteScheduledPipelines() {
        try {
            System.out.println("[Scheduler] Checking scheduled pipelines at " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            // Get all active scheduled pipelines
            List<Pipeline> scheduledPipelines = pipelineDao.findActiveScheduledPipelines();
            
            // Get current time
            LocalTime now = LocalTime.now();
            int currentHour = now.getHour();
            int currentMinute = now.getMinute();
            
            for (Pipeline pipeline : scheduledPipelines) {
                try {
                    // Parse schedule time
                    String scheduleTime = pipeline.getScheduleTime();
                    
                    if (scheduleTime == null || scheduleTime.isEmpty()) {
                        continue;
                    }
                    
                    // String handling: Parse schedule time
                    LocalTime scheduledTime = LocalTime.parse(scheduleTime, timeFormatter);
                    
                    // Check if it's time to run (within the same minute)
                    if (scheduledTime.getHour() == currentHour && 
                        scheduledTime.getMinute() == currentMinute) {
                        
                        System.out.println("[Scheduler] Executing scheduled pipeline: " + pipeline.getName());
                        
                        // Execute pipeline in a separate thread
                        executePipelineAsync(pipeline.getId());
                    }
                    
                } catch (Exception e) {
                    System.err.println("[Scheduler] Error checking pipeline " + pipeline.getId() + 
                        ": " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.err.println("[Scheduler] Error in check cycle: " + e.getMessage());
        }
    }

    /**
     * Execute a pipeline asynchronously
     * @param pipelineId Pipeline ID to execute
     */
    private void executePipelineAsync(Integer pipelineId) {
        // Submit to executor for async execution
        scheduler.submit(() -> {
            try {
                System.out.println("[Scheduler] Starting pipeline execution: " + pipelineId);
                
                PipelineExecutor.ExecutionResult result = pipelineExecutor.executePipeline(pipelineId);
                
                if (result.isSuccess()) {
                    System.out.println("[Scheduler] Pipeline " + pipelineId + " completed successfully");
                } else {
                    System.err.println("[Scheduler] Pipeline " + pipelineId + " failed: " + result.getMessage());
                }
                
            } catch (Exception e) {
                System.err.println("[Scheduler] Error executing pipeline " + pipelineId + ": " + e.getMessage());
            }
        });
    }

    /**
     * Manually trigger a pipeline execution
     * @param pipelineId Pipeline ID to execute
     */
    public void triggerPipeline(Integer pipelineId) {
        System.out.println("[Scheduler] Manual trigger for pipeline: " + pipelineId);
        executePipelineAsync(pipelineId);
    }

    /**
     * Check if scheduler is running
     * @return true if running
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Get scheduler status
     * @return Status string
     */
    public String getStatus() {
        if (isRunning) {
            return "Running - checking every 1 minute";
        } else {
            return "Stopped";
        }
    }

    /**
     * Get next scheduled run time for a pipeline
     * @param scheduleTime Schedule time string (HH:mm)
     * @return Formatted next run time
     */
    public String getNextRunTime(String scheduleTime) {
        if (scheduleTime == null || scheduleTime.isEmpty()) {
            return "Not scheduled";
        }
        
        try {
            LocalTime scheduled = LocalTime.parse(scheduleTime, timeFormatter);
            LocalTime now = LocalTime.now();
            LocalDateTime nextRun;
            
            if (scheduled.isAfter(now)) {
                // Today
                nextRun = LocalDateTime.now().with(scheduled);
            } else {
                // Tomorrow
                nextRun = LocalDateTime.now().plusDays(1).with(scheduled);
            }
            
            return nextRun.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            
        } catch (Exception e) {
            return "Invalid schedule";
        }
    }
}
