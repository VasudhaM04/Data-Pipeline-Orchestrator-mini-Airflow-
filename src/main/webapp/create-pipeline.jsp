<%-- 
    Create Pipeline Page
    Module: JSP, JSTL, String Handling (config parsing)
    Drag-and-drop style pipeline builder with DAG support
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create Pipeline - Data Pipeline Orchestrator</title>
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
        
        /* Task Builder */
        .task-builder {
            margin-top: 40px;
            border-top: 2px solid #e5e7eb;
            padding-top: 30px;
        }
        
        .task-builder h3 {
            font-size: 18px;
            color: #333;
            margin-bottom: 20px;
        }
        
        .task-list {
            display: flex;
            flex-direction: column;
            gap: 16px;
        }
        
        .task-card {
            background: #f9fafb;
            border: 2px solid #e5e7eb;
            border-radius: 8px;
            padding: 20px;
            position: relative;
        }
        
        .task-card:hover {
            border-color: #667eea;
        }
        
        .task-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 16px;
        }
        
        .task-number {
            background: #667eea;
            color: white;
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 600;
        }
        
        .btn-remove-task {
            background: #fee2e2;
            color: #991b1b;
            border: none;
            padding: 6px 12px;
            border-radius: 4px;
            cursor: pointer;
            font-size: 12px;
        }
        
        .task-form-row {
            display: grid;
            grid-template-columns: 2fr 1fr 1fr 1fr;
            gap: 12px;
        }
        
        .task-card input,
        .task-card select {
            padding: 10px;
            border: 1px solid #d1d5db;
            border-radius: 4px;
            font-size: 13px;
        }
        
        .task-card input:focus,
        .task-card select:focus {
            outline: none;
            border-color: #667eea;
        }
        
        .config-input {
            margin-top: 12px;
            width: 100%;
        }
        
        .config-input input {
            width: 100%;
            font-family: monospace;
            font-size: 12px;
        }
        
        .dependency-select {
            margin-top: 12px;
        }
        
        .dependency-select label {
            font-size: 12px;
            color: #666;
            margin-bottom: 4px;
            display: block;
        }
        
        .dependency-select select {
            width: 100%;
            padding: 8px;
            border: 1px solid #d1d5db;
            border-radius: 4px;
            font-size: 12px;
        }
        
        .btn-add-task {
            margin-top: 20px;
            background: white;
            color: #667eea;
            border: 2px dashed #667eea;
            padding: 16px;
            border-radius: 8px;
            cursor: pointer;
            font-size: 14px;
            font-weight: 500;
            width: 100%;
            transition: all 0.3s;
        }
        
        .btn-add-task:hover {
            background: #f5f3ff;
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
        
        .alert {
            padding: 12px;
            border-radius: 5px;
            margin-bottom: 20px;
            font-size: 14px;
        }
        
        .alert-error {
            background: #fee;
            color: #c33;
            border: 1px solid #fcc;
        }
        
        .help-text {
            font-size: 12px;
            color: #666;
            margin-top: 4px;
        }
        
        .dag-info {
            background: #f0f9ff;
            border: 1px solid #bae6fd;
            border-radius: 8px;
            padding: 16px;
            margin-bottom: 20px;
        }
        
        .dag-info h4 {
            color: #0369a1;
            font-size: 14px;
            margin-bottom: 8px;
        }
        
        .dag-info p {
            color: #0c4a6e;
            font-size: 13px;
            line-height: 1.5;
        }
    </style>
</head>
<body>
    <div class="header">
        <h1>Create New Pipeline</h1>
        <a href="${pageContext.request.contextPath}/dashboard" class="btn-back">&larr; Back to Dashboard</a>
    </div>
    
    <div class="main-content">
        <div class="form-container">
            <div class="form-header">
                <h2>Pipeline Configuration</h2>
            </div>
            
            <c:if test="${not empty error}">
                <div class="alert alert-error">
                    ${error}
                </div>
            </c:if>
            
            <div class="dag-info">
                <h4>DAG (Directed Acyclic Graph) Support</h4>
                <p>Tasks can depend on other tasks. Dependencies are resolved automatically at runtime. 
                   Select dependencies for each task to build your workflow. Tasks with no dependencies run first.</p>
            </div>
            
            <form action="${pageContext.request.contextPath}/pipeline/create" method="post" id="pipelineForm">
                <div class="form-row">
                    <div class="form-group">
                        <label for="name">Pipeline Name *</label>
                        <input type="text" id="name" name="name" required 
                               placeholder="e.g., Daily ETL Process">
                    </div>
                    
                    <div class="form-group">
                        <label for="scheduleTime">Schedule (HH:MM, optional)</label>
                        <input type="text" id="scheduleTime" name="scheduleTime" 
                               placeholder="e.g., 02:00" pattern="([0-1]?[0-9]|2[0-3]):[0-5][0-9]">
                        <span class="help-text">Leave empty for manual execution only</span>
                    </div>
                </div>
                
                <div class="form-group">
                    <label for="description">Description</label>
                    <textarea id="description" name="description" 
                              placeholder="Describe what this pipeline does..."></textarea>
                </div>
                
                <div class="task-builder">
                    <h3>Task Builder</h3>
                    
                    <div class="task-list" id="taskList">
                        <!-- Tasks will be added here dynamically -->
                    </div>
                    
                    <button type="button" class="btn-add-task" onclick="addTask()">
                        + Add Task
                    </button>
                </div>
                
                <div class="form-actions">
                    <button type="submit" class="btn-submit">Create Pipeline</button>
                    <a href="${pageContext.request.contextPath}/dashboard" class="btn-cancel">Cancel</a>
                </div>
            </form>
        </div>
    </div>
    
    <script>
        let taskCount = 0;
        
        function addTask() {
            taskCount++;
            const taskList = document.getElementById('taskList');
            
            const taskCard = document.createElement('div');
            taskCard.className = 'task-card';
            taskCard.id = 'task-' + taskCount;
            
            taskCard.innerHTML = `
                <div class="task-header">
                    <span class="task-number">Task ${taskCount}</span>
                    <button type="button" class="btn-remove-task" onclick="removeTask(${taskCount})">
                        Remove
                    </button>
                </div>
                <div class="task-form-row">
                    <input type="text" name="taskName[]" placeholder="Task Name" required>
                    <select name="taskType[]" required>
                        <option value="API">API Call</option>
                        <option value="DATABASE">Database</option>
                        <option value="CODE">Code/Script</option>
                        <option value="TRANSFORM">Transform</option>
                        <option value="LOAD">Load Data</option>
                    </select>
                    <input type="number" name="taskPriority[]" placeholder="Priority" value="0" min="0">
                    <input type="text" name="taskConfig[]" placeholder="Config (type=key;value)">
                </div>
                <div class="dependency-select">
                    <label>Depends On (select task indices, comma-separated)</label>
                    <input type="text" name="taskDependency[]" placeholder="e.g., 0,1 (task indices starting from 0)">
                </div>
            `;
            
            taskList.appendChild(taskCard);
        }
        
        function removeTask(id) {
            const taskCard = document.getElementById('task-' + id);
            if (taskCard) {
                taskCard.remove();
            }
        }
        
        // Add initial task
        addTask();
    </script>
</body>
</html>
