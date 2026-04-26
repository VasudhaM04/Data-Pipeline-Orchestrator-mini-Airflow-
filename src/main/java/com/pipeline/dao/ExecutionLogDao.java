package com.pipeline.dao;

import com.pipeline.model.ExecutionLog;
import com.pipeline.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ExecutionLog Data Access Object
 * Uses JDBC for low-level control over execution tracking
 * Module: JDBC (Module 4 - Advanced Java)
 */
public class ExecutionLogDao {

    // SQL Queries as String constants - String Handling concept
    private static final String INSERT_LOG = 
        "INSERT INTO execution_logs (task_id, status, message, start_time, end_time, duration_ms, created_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, NOW())";
    
    private static final String SELECT_BY_TASK = 
        "SELECT * FROM execution_logs WHERE task_id = ? ORDER BY created_at DESC";
    
    private static final String SELECT_BY_ID = 
        "SELECT * FROM execution_logs WHERE id = ?";
    
    private static final String SELECT_LATEST_BY_TASK = 
        "SELECT * FROM execution_logs WHERE task_id = ? ORDER BY created_at DESC LIMIT 1";
    
    private static final String UPDATE_LOG = 
        "UPDATE execution_logs SET status = ?, end_time = ?, duration_ms = ?, message = ? WHERE id = ?";
    
    private static final String DELETE_BY_TASK = 
        "DELETE FROM execution_logs WHERE task_id = ?";
    
    private static final String COUNT_BY_STATUS = 
        "SELECT COUNT(*) FROM execution_logs WHERE status = ?";

    /**
     * Create a new execution log entry using JDBC
     * @param log ExecutionLog to create
     * @return Created log with ID
     */
    public ExecutionLog create(ExecutionLog log) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(INSERT_LOG, Statement.RETURN_GENERATED_KEYS);
            
            // String handling: parsing and setting parameters
            ps.setInt(1, log.getTask().getId());
            ps.setString(2, log.getStatus());
            ps.setString(3, log.getMessage());
            ps.setString(4, log.getStartTime());
            ps.setString(5, log.getEndTime());
            ps.setObject(6, log.getDurationMs(), Types.BIGINT);
            
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating execution log failed, no rows affected.");
            }

            // Retrieve generated ID
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                log.setId(rs.getInt(1));
            }
            
            return log;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error creating execution log: " + e.getMessage(), e);
        } finally {
            closeResources(conn, ps, rs);
        }
    }

    /**
     * Find execution log by ID using JDBC
     * @param id Log ID
     * @return ExecutionLog or null if not found
     */
    public ExecutionLog findById(Integer id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(SELECT_BY_ID);
            ps.setInt(1, id);
            
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToLog(rs);
            }
            
            return null;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error finding execution log: " + e.getMessage(), e);
        } finally {
            closeResources(conn, ps, rs);
        }
    }

    /**
     * Find all execution logs for a task using JDBC
     * Module: Collections (List)
     * @param taskId Task ID
     * @return List of execution logs
     */
    public List<ExecutionLog> findByTaskId(Integer taskId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        // Collections Framework: List
        List<ExecutionLog> logs = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(SELECT_BY_TASK);
            ps.setInt(1, taskId);
            
            rs = ps.executeQuery();
            
            while (rs.next()) {
                logs.add(mapResultSetToLog(rs));
            }
            
            return logs;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error finding execution logs: " + e.getMessage(), e);
        } finally {
            closeResources(conn, ps, rs);
        }
    }

    /**
     * Get the latest execution log for a task
     * @param taskId Task ID
     * @return Latest ExecutionLog or null
     */
    public ExecutionLog findLatestByTaskId(Integer taskId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(SELECT_LATEST_BY_TASK);
            ps.setInt(1, taskId);
            
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToLog(rs);
            }
            
            return null;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error finding latest execution log: " + e.getMessage(), e);
        } finally {
            closeResources(conn, ps, rs);
        }
    }

    /**
     * Update execution log using JDBC
     * @param log ExecutionLog to update
     * @return Updated log
     */
    public ExecutionLog update(ExecutionLog log) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(UPDATE_LOG);
            
            ps.setString(1, log.getStatus());
            ps.setString(2, log.getEndTime());
            ps.setObject(3, log.getDurationMs(), Types.BIGINT);
            ps.setString(4, log.getMessage());
            ps.setInt(5, log.getId());
            
            ps.executeUpdate();
            
            return log;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error updating execution log: " + e.getMessage(), e);
        } finally {
            closeResources(conn, ps, null);
        }
    }

    /**
     * Delete all logs for a task
     * @param taskId Task ID
     * @return Number of deleted logs
     */
    public int deleteByTaskId(Integer taskId) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(DELETE_BY_TASK);
            ps.setInt(1, taskId);
            
            return ps.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting execution logs: " + e.getMessage(), e);
        } finally {
            closeResources(conn, ps, null);
        }
    }

    /**
     * Count logs by status
     * @param status Status to count
     * @return Count of logs
     */
    public Long countByStatus(String status) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(COUNT_BY_STATUS);
            ps.setString(1, status);
            
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getLong(1);
            }
            
            return 0L;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error counting execution logs: " + e.getMessage(), e);
        } finally {
            closeResources(conn, ps, rs);
        }
    }

    /**
     * Map ResultSet to ExecutionLog object
     * @param rs ResultSet
     * @return ExecutionLog
     * @throws SQLException
     */
    private ExecutionLog mapResultSetToLog(ResultSet rs) throws SQLException {
        ExecutionLog log = new ExecutionLog();
        log.setId(rs.getInt("id"));
        
        // Create minimal Task object with just ID
        com.pipeline.model.Task task = new com.pipeline.model.Task();
        task.setId(rs.getInt("task_id"));
        log.setTask(task);
        
        log.setStatus(rs.getString("status"));
        log.setMessage(rs.getString("message"));
        log.setStartTime(rs.getString("start_time"));
        log.setEndTime(rs.getString("end_time"));
        
        Long duration = rs.getLong("duration_ms");
        if (!rs.wasNull()) {
            log.setDurationMs(duration);
        }
        
        log.setCreatedAt(rs.getString("created_at"));
        
        return log;
    }

    /**
     * Close JDBC resources safely
     * @param conn Connection
     * @param ps PreparedStatement
     * @param rs ResultSet
     */
    private void closeResources(Connection conn, PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }
}
