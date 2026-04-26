<%-- 
    View Pipeline Page
    Module: JSP, JSTL
    Displays pipeline details and task execution graph
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>View Pipeline - ${pipeline.name}</title>
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
            max-width: 1200px;
            margin: 0 auto;
        }
        
        .pipeline-header {
            background: white;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            padding: 30px;
            margin-bottom: 30px;
        }
        
        .pipeline-header h2 {
            font-size: 24px;
            color: #333;
            margin-bottom: 10px;
        }
        
        .pipeline-meta {
            display: flex;
            gap: 30px;
            color: #666;
            font-size: 14px;
        }
        
        .status-badge {
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 500;
        }
        
        .status-success {
            background: #d1fae5;
            color: #065f46;
        }
        
        .status-running {
            background: #dbeafe;
            color: #1e40af;
        }
        
        .status-failed {
            background: #fee2e2;
            color: #991b1b;
        }
        
        .status-idle {
            background: #f3f4f6;
            color: #6b7280;
        }
        
        .tasks-section {
            background: white;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            padding: 30px;
        }
        
        .tasks-section h3 {
            font-size: 18px;
            color: #333;
            margin-bottom: 20px;
        }
        
        .dag-visualization {
            display: flex;
            flex-direction: column;
            gap: 20px;
            margin-top: 20px;
        }
        
        .task-node {
            background: #f9fafb;
            border: 2px solid #e5e7eb;
            border-radius: 8px;
            padding: 20px;
            position: relative;
            margin-left: 20px;
        }
        
        .task-node::before {
            content: '';
            position: absolute;
            left: -20px;
            top: 50%;
            width: 20px;
            height: 2px;
            background: #d1d5db;
        }
        
        .task-node.root::before {
            display: none;
        }
        
        .task-node.root {
            margin-left: 0;
        }
        
        .task-node:hover {
            border-color: #667eea;
        }
        
        .task-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 12px;
        }
        
        .task-name {
            font-size: 16px;
            font-weight: 600;
            color: #333;
        }
        
        .task-type {
            background: #e0e7ff;
            color: #4338ca;
            padding: 4px 10px;
            border-radius: 4px;
            font-size: 11px;
            font-weight: 500;
            text-transform: uppercase;
        }
        
        .task-details {
            font-size: 13px;
            color: #666;
        }
        
        .task-details p {
            margin-bottom: 4px;
        }
        
        .dependencies-list {
            margin-top: 8px;
        }
        
        .dependency-tag {
            display: inline-block;
            background: #fef3c7;
            color: #92400e;
            padding: 2px 8px;
            border-radius: 4px;
            font-size: 11px;
            margin-right: 4px;
            margin-bottom: 4px;
        }
        
        .empty-state {
            text-align: center;
            padding: 60px;
            color: #666;
        }
        
        .action-bar {
            display: flex;
            gap: 12px;
            margin-top: 30px;
        }
        
        .btn-run {
            background: linear-gradient(135deg, #10b981 0%, #059669 100%);
            color: white;
            padding: 12px 24px;
            border-radius: 5px;
            text-decoration: none;
            font-weight: 500;
        }
        
        .btn-edit {
            background: #f3f4f6;
            color: #374151;
            padding: 12px 24px;
            border-radius: 5px;
            text-decoration: none;
            font-weight: 500;
        }
        
        .btn-delete {
            background: #fee2e2;
            color: #991b1b;
            padding: 12px 24px;
            border-radius: 5px;
            text-decoration: none;
            font-weight: 500;
        }
    </style>
</head>
<body>
    <div class="header">
        <h1>Pipeline Details</h1>
        <a href="${pageContext.request.contextPath}/dashboard" class="btn-back">&larr; Back to Dashboard</a>
    </div>
    
    <div class="main-content">
        <div class="pipeline-header">
            <h2>${pipeline.name}</h2>
            <c:if test="${not empty pipeline.description}">
                <p style="color: #666; margin-top: 8px;">${pipeline.description}</p>
            </c:if>
            
            <div class="pipeline-meta" style="margin-top: 20px;">
                <div>
                    <strong>Status:</strong>
                    <c:choose>
                        <c:when test="${pipeline.status == 'SUCCESS'}">
                            <span class="status-badge status-success">Success</span>
                        </c:when>
                        <c:when test="${pipeline.status == 'RUNNING'}">
                            <span class="status-badge status-running">Running</span>
                        </c:when>
                        <c:when test="${pipeline.status == 'FAILED'}">
                            <span class="status-badge status-failed">Failed</span>
                        </c:when>
                        <c:otherwise>
                            <span class="status-badge status-idle">${pipeline.status}</span>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div>
                    <strong>Schedule:</strong>
                    <c:choose>
                        <c:when test="${not empty pipeline.scheduleTime}">
                            Daily at ${pipeline.scheduleTime}
                        </c:when>
                        <c:otherwise>
                            Manual only
                        </c:otherwise>
                    </c:choose>
                </div>
                <div>
                    <strong>Last Run:</strong>
                    <c:choose>
                        <c:when test="${not empty pipeline.lastRun}">
                            ${pipeline.lastRun}
                        </c:when>
                        <c:otherwise>
                            Never
                        </c:otherwise>
                    </c:choose>
                </div>
                <div>
                    <strong>Created:</strong> ${pipeline.createdAt}
                </div>
            </div>
            
            <div class="action-bar">
                <a href="${pageContext.request.contextPath}/pipeline/run?id=${pipeline.id}" 
                   class="btn-run">Run Now</a>
                <a href="${pageContext.request.contextPath}/pipeline/edit?id=${pipeline.id}" 
                   class="btn-edit">Edit Pipeline</a>
                <a href="${pageContext.request.contextPath}/pipeline/delete?id=${pipeline.id}" 
                   class="btn-delete"
                   onclick="return confirm('Are you sure you want to delete this pipeline?')">Delete</a>
            </div>
        </div>
        
        <div class="tasks-section">
            <h3>Task DAG (Execution Flow)</h3>
            
            <c:choose>
                <c:when test="${empty tasks}">
                    <div class="empty-state">
                        <p>No tasks configured for this pipeline.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="dag-visualization">
                        <c:forEach var="task" items="${tasks}">
                            <div class="task-node ${empty task.dependencies ? 'root' : ''}">
                                <div class="task-header">
                                    <span class="task-name">${task.name}</span>
                                    <span class="task-type">${task.type}</span>
                                </div>
                                <div class="task-details">
                                    <p><strong>Priority:</strong> ${task.priority}</p>
                                    <c:if test="${not empty task.config}">
                                        <p><strong>Config:</strong> <code>${task.config}</code></p>
                                    </c:if>
                                    
                                    <c:if test="${not empty task.dependencies}">
                                        <div class="dependencies-list">
                                            <strong>Depends on:</strong><br>
                                            <c:forEach var="dep" items="${task.dependencies}">
                                                <span class="dependency-tag">${dep.dependsOn.name}</span>
                                            </c:forEach>
                                        </div>
                                    </c:if>
                                    
                                    <c:if test="${empty task.dependencies}">
                                        <span class="dependency-tag" style="background: #d1fae5; color: #065f46;">
                                            No dependencies (root task)
                                        </span>
                                    </c:if>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</body>
</html>
