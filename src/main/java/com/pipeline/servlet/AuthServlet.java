package com.pipeline.servlet;

import com.pipeline.dao.UserDao;
import com.pipeline.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Authentication Servlet
 * 
 * CORE ADVANCED JAVA CONCEPTS:
 * - Servlets: HTTP request handling
 * - Session Tracking: HttpSession for user login persistence
 * - String Handling: Form parameter processing
 * 
 * Handles user login, logout, and registration.
 */
@WebServlet(urlPatterns = {"/login", "/logout", "/register"})
public class AuthServlet extends HttpServlet {

    private UserDao userDao;
    private DateTimeFormatter formatter;

    @Override
    public void init() throws ServletException {
        userDao = new UserDao();
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String path = request.getServletPath();
        
        switch (path) {
            case "/login":
                // Show login page
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                break;
                
            case "/logout":
                handleLogout(request, response);
                break;
                
            case "/register":
                // Show registration page
                request.getRequestDispatcher("/register.jsp").forward(request, response);
                break;
                
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();
        
        switch (path) {
            case "/login":
                handleLogin(request, response);
                break;
                
            case "/register":
                handleRegister(request, response);
                break;
                
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Handle user login
     * Module: Session Tracking
     */
    private void handleLogin(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, ServletException {
        
        // String handling: Get form parameters
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        // Validation
        if (email == null || email.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            
            request.setAttribute("error", "Email and password are required");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }
        
        // String handling: trim and normalize
        email = email.trim().toLowerCase();
        
        // Authenticate user
        User user = userDao.validateLogin(email, password);
        
        if (user != null) {
            // Session Tracking: Create session and store user
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getId());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userName", user.getFullName());
            
            // Set session timeout (30 minutes)
            session.setMaxInactiveInterval(30 * 60);
            
            System.out.println("[Auth] User logged in: " + user.getEmail());
            
            // Redirect to dashboard
            response.sendRedirect(request.getContextPath() + "/dashboard");
            
        } else {
            request.setAttribute("error", "Invalid email or password");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }

    /**
     * Handle user logout
     * Module: Session Tracking
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            // Log logout
            String userEmail = (String) session.getAttribute("userEmail");
            System.out.println("[Auth] User logged out: " + userEmail);
            
            // Invalidate session
            session.invalidate();
        }
        
        // Redirect to login page
        response.sendRedirect(request.getContextPath() + "/login");
    }

    /**
     * Handle user registration
     */
    private void handleRegister(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, ServletException {
        
        // Get form parameters
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        
        // Validation
        if (fullName == null || fullName.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            
            request.setAttribute("error", "All fields are required");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }
        
        // String handling: normalize
        fullName = fullName.trim();
        email = email.trim().toLowerCase();
        
        // Check password match
        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Passwords do not match");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }
        
        // Check if user already exists
        if (userDao.findByEmail(email) != null) {
            request.setAttribute("error", "Email already registered");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }
        
        // Create new user
        User user = new User(email, password, fullName);
        user.setCreatedAt(LocalDateTime.now().format(formatter));
        
        try {
            userDao.create(user);
            System.out.println("[Auth] New user registered: " + email);
            
            // Redirect to login with success message
            request.setAttribute("success", "Registration successful. Please login.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            
        } catch (Exception e) {
            request.setAttribute("error", "Registration failed: " + e.getMessage());
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }
    }
}
