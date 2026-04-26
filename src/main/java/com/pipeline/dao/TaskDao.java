package com.pipeline.dao;

import com.pipeline.model.Task;
import com.pipeline.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Task Data Access Object
 * Handles database operations for Task entity using Hibernate ORM
 * Module: Hibernate ORM + Collections (List, Set)
 */
public class TaskDao {

    /**
     * Create a new task
     * @param task Task to create
     * @return Created task with ID
     */
    public Task create(Task task) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(task);
            transaction.commit();
            return task;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error creating task: " + e.getMessage(), e);
        }
    }

    /**
     * Find task by ID
     * @param id Task ID
     * @return Task or null if not found
     */
    public Task findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Task.class, id);
        }
    }

    /**
     * Find task by ID with dependencies loaded
     * @param id Task ID
     * @return Task with dependencies or null if not found
     */
    public Task findByIdWithDependencies(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Task> query = session.createQuery(
                "SELECT t FROM Task t LEFT JOIN FETCH t.dependencies WHERE t.id = :id", 
                Task.class);
            query.setParameter("id", id);
            List<Task> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        }
    }

    /**
     * Find all tasks for a pipeline
     * @param pipelineId Pipeline ID
     * @return List of tasks
     */
    public List<Task> findByPipelineId(Integer pipelineId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Task> query = session.createQuery(
                "FROM Task WHERE pipeline.id = :pipelineId ORDER BY priority DESC", 
                Task.class);
            query.setParameter("pipelineId", pipelineId);
            return query.list();
        }
    }

    /**
     * Find all tasks for a pipeline with dependencies
     * @param pipelineId Pipeline ID
     * @return Set of tasks with dependencies
     */
    public Set<Task> findByPipelineIdWithDependencies(Integer pipelineId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Task> query = session.createQuery(
                "SELECT DISTINCT t FROM Task t " +
                "LEFT JOIN FETCH t.dependencies d " +
                "LEFT JOIN FETCH d.dependsOn " +
                "WHERE t.pipeline.id = :pipelineId", 
                Task.class);
            query.setParameter("pipelineId", pipelineId);
            return new HashSet<>(query.list());
        }
    }

    /**
     * Find all tasks
     * @return List of all tasks
     */
    public List<Task> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Task", Task.class).list();
        }
    }

    /**
     * Update task
     * @param task Task to update
     * @return Updated task
     */
    public Task update(Task task) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.update(task);
            transaction.commit();
            return task;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error updating task: " + e.getMessage(), e);
        }
    }

    /**
     * Update task status
     * @param id Task ID
     * @param status New status
     */
    public void updateStatus(Integer id, String status) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Query<?> query = session.createQuery(
                "UPDATE Task SET status = :status WHERE id = :id");
            query.setParameter("status", status);
            query.setParameter("id", id);
            query.executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error updating task status: " + e.getMessage(), e);
        }
    }

    /**
     * Reset all task statuses for a pipeline
     * @param pipelineId Pipeline ID
     */
    public void resetTaskStatuses(Integer pipelineId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Query<?> query = session.createQuery(
                "UPDATE Task SET status = 'PENDING' WHERE pipeline.id = :pipelineId");
            query.setParameter("pipelineId", pipelineId);
            query.executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error resetting task statuses: " + e.getMessage(), e);
        }
    }

    /**
     * Delete task by ID
     * @param id Task ID to delete
     * @return true if deleted
     */
    public boolean delete(Integer id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Task task = session.get(Task.class, id);
            if (task != null) {
                session.delete(task);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error deleting task: " + e.getMessage(), e);
        }
    }

    /**
     * Count tasks by pipeline
     * @param pipelineId Pipeline ID
     * @return Count of tasks
     */
    public Long countByPipelineId(Integer pipelineId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(t) FROM Task t WHERE t.pipeline.id = :pipelineId", Long.class);
            query.setParameter("pipelineId", pipelineId);
            return query.uniqueResult();
        }
    }

    /**
     * Count tasks by status for a pipeline
     * @param pipelineId Pipeline ID
     * @param status Task status
     * @return Count of tasks
     */
    public Long countByStatus(Integer pipelineId, String status) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(t) FROM Task t WHERE t.pipeline.id = :pipelineId AND t.status = :status", 
                Long.class);
            query.setParameter("pipelineId", pipelineId);
            query.setParameter("status", status);
            return query.uniqueResult();
        }
    }
}
