<%-- 
    Dashboard Page
    Module: JSP, JSTL, Session Tracking
    Displays user's pipelines and statistics
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - Data Pipeline Orchestrator</title>
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
        
        /* Header */
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px 40px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .header h1 {
            font-size: 24px;
        }
        
        .user-info {
            display: flex;
            align-items: center;
            gap: 20px;
        }
        
        .user-info span {
            font-size: 14px;
        }
        
        .btn-logout {
            background: rgba(255,255,255,0.2);
            color: white;
            border: 1px solid white;
            padding: 8px 16px;
            border-radius: 5px;
            text-decoration: none;
            font-size: 14px;
            transition: background 0.3s;
        }
        
        .btn-logout:hover {
            background: rgba(255,255,255,0.3);
        }
        
        /* Main Content */
        .main-content {
            padding: 40px;
            max-width: 1400px;
            margin: 0 auto;
        }
        
        /* Stats Cards */
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(5, 1fr);
            gap: 20px;
            margin-bottom: 40px;
        }
        
        .stat-card {
            background: white;
            padding: 24px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        
        .stat-card h3 {
            font-size: 14px;
            color: #666;
            margin-bottom: 8px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        
        .stat-card .number {
            font-size: 32px;
            font-weight: 700;
            color: #333;
        }
        
        .stat-card.success .number { color: #10b981; }
        .stat-card.warning .number { color: #f59e0b; }
        .stat-card.danger .number { color: #ef4444; }
        
        /* Pipelines Section */
        .pipelines-section {
            background: white;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            overflow: hidden;
        }
        
        .section-header {
            padding: 24px;
            border-bottom: 1px solid #e5e7eb;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .section-header h2 {
            font-size: 20px;
            color: #333;
        }
        
        .btn-create {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 12px 24px;
            border-radius: 5px;
            text-decoration: none;
            font-weight: 500;
            transition: transform 0.2s, box-shadow 0.2s;
        }
        
        .btn-create:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
        }
        
        /* Table */
        .pipelines-table {
            width: 100%;
            border-collapse: collapse;
        }
        
        .pipelines-table th,
        .pipelines-table td {
            padding: 16px 24px;
            text-align: left;
        }
        
        .pipelines-table th {
            background: #f9fafb;
            color: #666;
            font-size: 12px;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        
        .pipelines-table tr {
            border-bottom: 1px solid #e5e7eb;
        }
        
        .pipelines-table tr:last-child {
            border-bottom: none;
        }
        
        .pipelines-table td {
            font-size: 14px;
            color: #374151;
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
        
        .status-partial {
            background: #fef3c7;
            color: #92400e;
        }
        
        .actions {
            display: flex;
            gap: 8px;
        }
        
        .btn-action {
            padding: 6px 12px;
            border-radius: 4px;
            font-size: 12px;
            text-decoration: none;
            transition: background 0.2s;
        }
        
        .btn-view {
            background: #f3f4f6;
            color: #374151;
        }
        
        .btn-view:hover {
            background: #e5e7eb;
        }
        
        .btn-run {
            background: #d1fae5;
            color: #065f46;
        }
        
        .btn-run:hover {
            background: #a7f3d0;
        }
        
        .btn-delete {
            background: #fee2e2;
            color: #991b1b;
        }
        
        .btn-delete:hover {
            background: #fecaca;
        }
        
        .empty-state {
            text-align: center;
            padding: 60px 20px;
            color: #666;
        }
        
        .empty-state h3 {
            margin-bottom: 12px;
            color: #333;
        }
        
        /* Alert */
        .alert {
            padding: 16px 24px;
            background: #d1fae5;
            color: #065f46;
            margin-bottom: 20px;
            border-radius: 8px;
        }
    </style>
</head>
<body>
    <div class="header">
        <h1>Pipeline Orchestrator</h1>
        <div class="user-info">
            <span>Welcome, ${sessionScope.userName}</span>
            <a href="${pageContext.request.contextPath}/logout" class="btn-logout">Logout</a>
        </div>
    </div>
    
    <div class="main-content">
        <c:if test="${not empty sessionScope.message}">
            <div class="alert">
                ${sessionScope.message}
                <c:remove var="message" scope="session"/>
            </div>
        </c:if>
        
        <!-- Statistics -->
        <div class="stats-grid">
            <div class="stat-card">
                <h3>Total Pipelines</h3>
                <div class="number">${totalPipelines}</div>
            </div>
            <div class="stat-card success">
                <h3>Active</h3>
                <div class="number">${activePipelines}</div>
            </div>
            <div class="stat-card">
                <h3>Scheduled</h3>
                <div class="number">${scheduledPipelines}</div>
            </div>
            <div class="stat-card success">
                <h3>Success</h3>
                <div class="number">${successRuns}</div>
            </div>
            <div class="stat-card danger">
                <h3>Failed</h3>
                <div class="number">${failedRuns}</div>
            </div>
        </div>
        
        <!-- Pipelines Table -->
        <div class="pipelines-section">
            <div class="section-header">
                <h2>Your Pipelines</h2>
                <a href="${pageContext.request.contextPath}/pipeline/create" class="btn-create">
                    + Create Pipeline
                </a>
            </div>
            
            <c:choose>
                <c:when test="${empty pipelines}">
                    <div class="empty-state">
                        <h3>No pipelines yet</h3>
                        <p>Create your first pipeline to get started</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <table class="pipelines-table">
                        <thead>
                            <tr>
                                <th>Name</th>
                                <th>Status</th>
                                <th>Schedule</th>
                                <th>Last Run</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="pipeline" items="${pipelines}">
                                <tr>
                                    <td>
                                        <strong>${pipeline.name}</strong>
                                        <c:if test="${not empty pipeline.description}">
                                            <br><small style="color: #666;">${pipeline.description}</small>
                                        </c:if>
                                    </td>
                                    <td>
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
                                            <c:when test="${pipeline.status == 'PARTIAL_FAILURE'}">
                                                <span class="status-badge status-partial">Partial</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="status-badge status-idle">${pipeline.status}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty pipeline.scheduleTime}">
                                                Daily at ${pipeline.scheduleTime}
                                            </c:when>
                                            <c:otherwise>
                                                - (Manual)
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty pipeline.lastRun}">
                                                ${pipeline.lastRun}
                                            </c:when>
                                            <c:otherwise>
                                                Never
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <div class="actions">
                                            <a href="${pageContext.request.contextPath}/pipeline/view?id=${pipeline.id}" 
                                               class="btn-action btn-view">View</a>
                                            <a href="${pageContext.request.contextPath}/pipeline/run?id=${pipeline.id}" 
                                               class="btn-action btn-run">Run</a>
                                            <a href="${pageContext.request.contextPath}/pipeline/delete?id=${pipeline.id}" 
                                               class="btn-action btn-delete"
                                               onclick="return confirm('Delete this pipeline?')">Delete</a>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</body>
</html>
