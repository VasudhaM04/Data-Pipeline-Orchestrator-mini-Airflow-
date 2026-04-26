package com.pipeline.servlet;

import com.pipeline.dao.*;
import com.pipeline.model.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Pipeline Servlet
 * 
 * CORE ADVANCED JAVA CONCEPTS:
 * - Servlets: CRUD operations
 * - Collections: Map, List for task dependencies
 * - String Handling: Config parsing
 * 
 * Handles pipeline CRUD and execution.
 */
@WebServlet(urlPatterns = {"/pipeline/create", "/pipeline/edit", "/pipeline/delete", "/pipeline/view", "/pipeline/run"})
public class PipelineServlet extends HttpServlet {

    private PipelineDao pipelineDao;
    private TaskDao taskDao;
    private DependencyDao dependencyDao;
    private com.pipeline.service.PipelineExecutor pipelineExecutor;
    private com.pipeline.service.PipelineScheduler pipelineScheduler;
    private DateTimeFormatter formatter;

    @Override
    public void init() throws ServletException {
        pipelineDao = new PipelineDao();
        taskDao = new TaskDao();
        dependencyDao = new DependencyDao();
        pipelineExecutor = new com.pipeline.service.PipelineExecutor();
        pipelineScheduler = com.pipeline.service.PipelineScheduler.getInstance();
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check authentication
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        String path = request.getServletPath();
        
        switch (path) {
            case "/pipeline/create":
                // Show create pipeline page
                request.getRequestDispatcher("/create-pipeline.jsp").forward(request, response);
                break;
                
            case "/pipeline/edit":
                handleEditView(request, response);
                break;
                
            case "/pipeline/delete":
                handleDelete(request, response);
                break;
                
            case "/pipeline/view":
                handleView(request, response);
                break;
                
            case "/pipeline/run":
                handleRun(request, response);
                break;
                
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        
        // Check authentication
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        String path = request.getServletPath();
        
        switch (path) {
            case "/pipeline/create":
                handleCreate(request, response);
                break;
                
            case "/pipeline/edit":
                handleUpdate(request, response);
                break;
                
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Handle pipeline creation
     * Module: Collections (Map for task parsing)
     */
    private void handleCreate(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        
        // Get form parameters
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String scheduleTime = request.getParameter("scheduleTime");
        
        // Get task data
        String[] taskNames = request.getParameterValues("taskName[]");
        String[] taskTypes = request.getParameterValues("taskType[]");
        String[] taskConfigs = request.getParameterValues("taskConfig[]");
        String[] taskPriorities = request.getParameterValues("taskPriority[]");
        String[] taskDependencies = request.getParameterValues("taskDependency[]");
        
        // Validate
        if (name == null || name.trim().isEmpty()) {
            request.setAttribute("error", "Pipeline name is required");
            request.getRequestDispatcher("/create-pipeline.jsp").forward(request, response);
            return;
        }
        
        // Create pipeline
        Pipeline pipeline = new Pipeline();
        pipeline.setName(name.trim());
        pipeline.setDescription(description);
        pipeline.setScheduleTime(scheduleTime);
        pipeline.setIsActive(true);
        pipeline.setCreatedAt(LocalDateTime.now().format(formatter));
        pipeline.setStatus("IDLE");
        pipeline.setUser(user);
        
        try {
            // Save pipeline
            pipelineDao.create(pipeline);
            
            // Create tasks
            if (taskNames != null) {
                // Collections: Map to track tasks by index for dependency resolution
                Map<Integer, Task> taskMap = new HashMap<>();
                
                for (int i = 0; i < taskNames.length; i++) {
                    Task task = new Task();
                    task.setName(taskNames[i].trim());
                    task.setType(taskTypes[i]);
                    task.setConfig(taskConfigs[i]);
                    task.setPriority(parsePriority(taskPriorities[i]));
                    task.setCreatedAt(LocalDateTime.now().format(formatter));
                    task.setPipeline(pipeline);
                    
                    taskDao.create(task);
                    taskMap.put(i, task);
                }
                
                // Create dependencies
                if (taskDependencies != null) {
                    for (int i = 0; i < taskDependencies.length; i++) {
                        String depString = taskDependencies[i];
                        
                        // String handling: Parse comma-separated dependency indices
                        if (depString != null && !depString.trim().isEmpty()) {
                            String[] depIndices = depString.split(",");
                            
                            for (String depIndex : depIndices) {
                                depIndex = depIndex.trim();
                                if (!depIndex.isEmpty()) {
                                    try {
                                        int depIdx = Integer.parseInt(depIndex);
                                        Task task = taskMap.get(i);
                                        Task dependsOn = taskMap.get(depIdx);
                                        
                                        if (task != null && dependsOn != null) {
                                            Dependency dependency = new Dependency(task, dependsOn);
                                            dependencyDao.create(dependency);
                                        }
                                    } catch (NumberFormatException e) {
                                        // Invalid index, skip
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            System.out.println("[Pipeline] Created pipeline: " + pipeline.getName());
            response.sendRedirect(request.getContextPath() + "/dashboard");
            
        } catch (Exception e) {
            request.setAttribute("error", "Error creating pipeline: " + e.getMessage());
            request.getRequestDispatcher("/create-pipeline.jsp").forward(request, response);
        }
    }

    /**
     * Handle pipeline update
     */
    private void handleUpdate(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pipelineIdStr = request.getParameter("id");
        
        if (pipelineIdStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        try {
            Integer pipelineId = Integer.parseInt(pipelineIdStr);
            Pipeline pipeline = pipelineDao.findById(pipelineId);
            
            if (pipeline == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // Update fields
            pipeline.setName(request.getParameter("name"));
            pipeline.setDescription(request.getParameter("description"));
            pipeline.setScheduleTime(request.getParameter("scheduleTime"));
            
            String isActive = request.getParameter("isActive");
            pipeline.setIsActive(isActive != null);
            
            pipelineDao.update(pipeline);
            
            response.sendRedirect(request.getContextPath() + "/dashboard");
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Handle pipeline deletion
     */
    private void handleDelete(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String pipelineIdStr = request.getParameter("id");
        
        if (pipelineIdStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        try {
            Integer pipelineId = Integer.parseInt(pipelineIdStr);
            
            // Delete dependencies first
            List<Dependency> deps = dependencyDao.findByPipelineId(pipelineId);
            for (Dependency dep : deps) {
                dependencyDao.delete(dep.getId());
            }
            
            // Delete tasks
            List<Task> tasks = taskDao.findByPipelineId(pipelineId);
            for (Task task : tasks) {
                taskDao.delete(task.getId());
            }
            
            // Delete pipeline
            pipelineDao.delete(pipelineId);
            
            System.out.println("[Pipeline] Deleted pipeline: " + pipelineId);
            response.sendRedirect(request.getContextPath() + "/dashboard");
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Handle pipeline view
     */
    private void handleView(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pipelineIdStr = request.getParameter("id");
        
        if (pipelineIdStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        try {
            Integer pipelineId = Integer.parseInt(pipelineIdStr);
            
            // Load pipeline with tasks and dependencies
            Pipeline pipeline = pipelineDao.findByIdWithTasks(pipelineId);
            
            if (pipeline == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // Get tasks with dependencies
            Set<Task> tasks = taskDao.findByPipelineIdWithDependencies(pipelineId);
            
            request.setAttribute("pipeline", pipeline);
            request.setAttribute("tasks", tasks);
            
            request.getRequestDispatcher("/view-pipeline.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Handle edit view
     */
    private void handleEditView(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pipelineIdStr = request.getParameter("id");
        
        if (pipelineIdStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        try {
            Integer pipelineId = Integer.parseInt(pipelineIdStr);
            Pipeline pipeline = pipelineDao.findByIdWithTasks(pipelineId);
            
            if (pipeline == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            request.setAttribute("pipeline", pipeline);
            request.getRequestDispatcher("/edit-pipeline.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Handle pipeline run
     */
    private void handleRun(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String pipelineIdStr = request.getParameter("id");
        
        if (pipelineIdStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        try {
            Integer pipelineId = Integer.parseInt(pipelineIdStr);
            
            // Trigger execution via scheduler
            pipelineScheduler.triggerPipeline(pipelineId);
            
            System.out.println("[Pipeline] Triggered execution for pipeline: " + pipelineId);
            
            // Redirect back to dashboard
            response.sendRedirect(request.getContextPath() + "/dashboard");
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Parse priority string to integer
     */
    private Integer parsePriority(String priorityStr) {
        if (priorityStr == null || priorityStr.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(priorityStr.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
