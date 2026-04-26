<%-- 
    Edit Pipeline Page
    Module: JSP, JSTL
    Edit existing pipeline configuration
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit Pipeline - ${pipeline.name}</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #f5f7fa;
            min-height: 100vh;
        }
        
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px 40px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .header h1 {
            font-size: 20px;
        }
        
        .btn-back {
            color: white;
            text-decoration: none;
            font-size: 14px;
            opacity: 0.9;
        }
        
        .main-content {
            padding: 40px;
            max-width: 800px;
            margin: 0 auto;
        }
        
        .form-container {
            background: white;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            padding: 40px;
        }
        
        .form-header {
            margin-bottom: 30px;
        }
        
        .form-header h2 {
            font-size: 24px;
            color: #333;
        }
        
        .form-group {
            margin-bottom: 24px;
        }
        
        .form-group label {
            display: block;
            margin-bottom: 8px;
            color: #333;
            font-weight: 500;
        }
        
        .form-group input,
        .form-group textarea {
            width: 100%;
            padding: 12px;
            border: 2px solid #e0e0e0;
            border-radius: 5px;
            font-size: 14px;
            transition: border-color 0.3s;
        }
        
        .form-group input:focus,
        .form-group textarea:focus {
            outline: none;
            border-color: #667eea;
        }
        
        .form-group textarea {
            resize: vertical;
            min-height: 80px;
        }
        
        .form-row {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
        }
        
        .checkbox-group {
            display: flex;
            align-items: center;
            gap: 10px;
        }
        
        .checkbox-group input {
            width: auto;
        }
        
        .checkbox-group label {
            margin-bottom: 0;
        }
        
        .form-actions {
            margin-top: 40px;
            display: flex;
            gap: 16px;
        }
        
        .btn-submit {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            padding: 14px 32px;
            border-radius: 5px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: transform 0.2s, box-shadow 0.2s;
        }
        
        .btn-submit:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
        }
        
        .btn-cancel {
            background: #f3f4f6;
            color: #374151;
            border: none;
            padding: 14px 32px;
            border-radius: 5px;
            font-size: 16px;
            font-weight: 500;
            cursor: pointer;
            text-decoration: none;
        }
        
        .info-box {
            background: #f0f9ff;
            border: 1px solid #bae6fd;
            border-radius: 8px;
            padding: 16px;
            margin-bottom: 24px;
        }
        
        .info-box p {
            color: #0c4a6e;
            font-size: 13px;
        }
        
        .tasks-list {
            background: #f9fafb;
            border-radius: 8px;
            padding: 20px;
            margin-top: 10px;
        }
        
        .task-item {
            padding: 12px;
            border-bottom: 1px solid #e5e7eb;
            font-size: 14px;
        }
        
        .task-item:last-child {
            border-bottom: none;
        }
        
        .task-item strong {
            color: #333;
        }
        
        .task-item small {
            color: #666;
        }
    </style>
</head>
<body>
    <div class="header">
        <h1>Edit Pipeline</h1>
        <a href="${pageContext.request.contextPath}/dashboard" class="btn-back">&larr; Back to Dashboard</a>
    </div>
    
    <div class="main-content">
        <div class="form-container">
            <div class="form-header">
                <h2>Edit Pipeline: ${pipeline.name}</h2>
            </div>
            
            <div class="info-box">
                <p><strong>Note:</strong> Task configuration cannot be edited here. 
                   To modify tasks, delete this pipeline and create a new one with the desired configuration.</p>
            </div>
            
            <form action="${pageContext.request.contextPath}/pipeline/edit" method="post">
                <input type="hidden" name="id" value="${pipeline.id}">
                
                <div class="form-row">
                    <div class="form-group">
                        <label for="name">Pipeline Name *</label>
                        <input type="text" id="name" name="name" required 
                               value="${pipeline.name}">
                    </div>
                    
                    <div class="form-group">
                        <label for="scheduleTime">Schedule (HH:MM)</label>
                        <input type="text" id="scheduleTime" name="scheduleTime" 
                               value="${pipeline.scheduleTime}" 
                               pattern="([0-1]?[0-9]|2[0-3]):[0-5][0-9]">
                    </div>
                </div>
                
                <div class="form-group">
                    <label for="description">Description</label>
                    <textarea id="description" name="description">${pipeline.description}</textarea>
                </div>
                
                <div class="form-group checkbox-group">
                    <input type="checkbox" id="isActive" name="isActive" value="true" 
                           ${pipeline.isActive ? 'checked' : ''}>
                    <label for="isActive">Pipeline Active (enable scheduling)</label>
                </div>
                
                <div class="form-group">
                    <label>Current Tasks (${pipeline.tasks.size()})</label>
                    <div class="tasks-list">
                        <c:forEach var="task" items="${pipeline.tasks}">
                            <div class="task-item">
                                <strong>${task.name}</strong> 
                                <span style="background: #e0e7ff; color: #4338ca; padding: 2px 8px; border-radius: 4px; font-size: 11px; text-transform: uppercase;">
                                    ${task.type}
                                </span>
                                <br>
                                <small>Priority: ${task.priority}</small>
                                <c:if test="${not empty task.config}">
                                    <br><small>Config: ${task.config}</small>
                                </c:if>
                            </div>
                        </c:forEach>
                    </div>
                </div>
                
                <div class="form-actions">
                    <button type="submit" class="btn-submit">Update Pipeline</button>
                    <a href="${pageContext.request.contextPath}/dashboard" class="btn-cancel">Cancel</a>
                </div>
            </form>
        </div>
    </div>
</body>
</html>
