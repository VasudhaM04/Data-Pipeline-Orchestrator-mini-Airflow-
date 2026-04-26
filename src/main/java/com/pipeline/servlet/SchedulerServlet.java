package com.pipeline.servlet;

import com.pipeline.service.PipelineScheduler;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * Scheduler Servlet
 * 
 * CORE ADVANCED JAVA CONCEPTS:
 * - Servlets: System control endpoint
 * - Multithreading: Scheduler start/stop
 * 
 * Controls the pipeline scheduler service.
 */
@WebServlet(urlPatterns = {"/scheduler/start", "/scheduler/stop", "/scheduler/status"})
public class SchedulerServlet extends HttpServlet {

    private PipelineScheduler scheduler;

    @Override
    public void init() throws ServletException {
        scheduler = PipelineScheduler.getInstance();
        
        // Auto-start scheduler on servlet init
        if (!scheduler.isRunning()) {
            scheduler.start();
            System.out.println("[SchedulerServlet] Auto-started scheduler");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check authentication (optional for status)
        HttpSession session = request.getSession(false);
        
        String path = request.getServletPath();
        
        switch (path) {
            case "/scheduler/start":
                if (session == null || session.getAttribute("user") == null) {
                    response.sendRedirect(request.getContextPath() + "/login");
                    return;
                }
                handleStart(request, response);
                break;
                
            case "/scheduler/stop":
                if (session == null || session.getAttribute("user") == null) {
                    response.sendRedirect(request.getContextPath() + "/login");
                    return;
                }
                handleStop(request, response);
                break;
                
            case "/scheduler/status":
                handleStatus(request, response);
                break;
                
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Start the scheduler
     */
    private void handleStart(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        scheduler.start();
        
        request.getSession().setAttribute("message", "Scheduler started successfully");
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }

    /**
     * Stop the scheduler
     */
    private void handleStop(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        scheduler.stop();
        
        request.getSession().setAttribute("message", "Scheduler stopped");
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }

    /**
     * Get scheduler status
     */
    private void handleStatus(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String json = String.format(
            "{\"running\": %b, \"status\": \"%s\"}",
            scheduler.isRunning(),
            scheduler.getStatus()
        );
        
        response.getWriter().write(json);
    }

    @Override
    public void destroy() {
        // Stop scheduler when servlet is destroyed
        if (scheduler != null) {
            scheduler.stop();
            System.out.println("[SchedulerServlet] Stopped scheduler on destroy");
        }
    }
}
