package com.pipeline.servlet;

import com.pipeline.dao.PipelineDao;
import com.pipeline.model.Pipeline;
import com.pipeline.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Dashboard Servlet
 * 
 * CORE ADVANCED JAVA CONCEPTS:
 * - Servlets: Controller for MVC
 * - Session Tracking: User authentication check
 * - Collections: List for pipeline data
 * 
 * Displays the main dashboard with user's pipelines.
 */
@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private PipelineDao pipelineDao;

    @Override
    public void init() throws ServletException {
        pipelineDao = new PipelineDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Session Tracking: Check if user is logged in
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("user") == null) {
            // Not logged in, redirect to login
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        // Get user from session
        User user = (User) session.getAttribute("user");
        
        // Load user's pipelines
        List<Pipeline> pipelines = pipelineDao.findByUserId(user.getId());
        
        // Calculate statistics
        int totalPipelines = pipelines.size();
        int activePipelines = 0;
        int scheduledPipelines = 0;
        int successRuns = 0;
        int failedRuns = 0;
        
        for (Pipeline pipeline : pipelines) {
            if (pipeline.getIsActive()) {
                activePipelines++;
            }
            if (pipeline.getScheduleTime() != null && !pipeline.getScheduleTime().isEmpty()) {
                scheduledPipelines++;
            }
            if ("SUCCESS".equals(pipeline.getStatus())) {
                successRuns++;
            } else if ("FAILED".equals(pipeline.getStatus())) {
                failedRuns++;
            }
        }
        
        // Set attributes for JSP
        request.setAttribute("pipelines", pipelines);
        request.setAttribute("totalPipelines", totalPipelines);
        request.setAttribute("activePipelines", activePipelines);
        request.setAttribute("scheduledPipelines", scheduledPipelines);
        request.setAttribute("successRuns", successRuns);
        request.setAttribute("failedRuns", failedRuns);
        request.setAttribute("user", user);
        
        // Forward to dashboard JSP
        request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
    }
}
