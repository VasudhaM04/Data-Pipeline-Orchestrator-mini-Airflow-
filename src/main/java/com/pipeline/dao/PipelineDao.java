package com.pipeline.dao;

import com.pipeline.model.Pipeline;
import com.pipeline.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

/**
 * Pipeline Data Access Object
 * Handles database operations for Pipeline entity using Hibernate ORM
 * Module: Hibernate ORM + Collections (List)
 */
public class PipelineDao {

    /**
     * Create a new pipeline
     * @param pipeline Pipeline to create
     * @return Created pipeline with ID
     */
    public Pipeline create(Pipeline pipeline) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(pipeline);
            transaction.commit();
            return pipeline;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error creating pipeline: " + e.getMessage(), e);
        }
    }

    /**
     * Find pipeline by ID
     * @param id Pipeline ID
     * @return Pipeline or null if not found
     */
    public Pipeline findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Pipeline.class, id);
        }
    }

    /**
     * Find pipeline by ID with all tasks loaded
     * @param id Pipeline ID
     * @return Pipeline with tasks or null if not found
     */
    public Pipeline findByIdWithTasks(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Pipeline> query = session.createQuery(
                "SELECT p FROM Pipeline p LEFT JOIN FETCH p.tasks WHERE p.id = :id", 
                Pipeline.class);
            query.setParameter("id", id);
            List<Pipeline> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        }
    }

    /**
     * Find all pipelines for a user
     * @param userId User ID
     * @return List of pipelines
     */
    public List<Pipeline> findByUserId(Integer userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Pipeline> query = session.createQuery(
                "FROM Pipeline WHERE user.id = :userId ORDER BY createdAt DESC", 
                Pipeline.class);
            query.setParameter("userId", userId);
            return query.list();
        }
    }

    /**
     * Find all active pipelines with schedules
     * @return List of active pipelines
     */
    public List<Pipeline> findActiveScheduledPipelines() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Pipeline> query = session.createQuery(
                "FROM Pipeline WHERE isActive = true AND scheduleTime IS NOT NULL", 
                Pipeline.class);
            return query.list();
        }
    }

    /**
     * Find all pipelines
     * @return List of all pipelines
     */
    public List<Pipeline> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Pipeline ORDER BY createdAt DESC", Pipeline.class).list();
        }
    }

    /**
     * Update pipeline
     * @param pipeline Pipeline to update
     * @return Updated pipeline
     */
    public Pipeline update(Pipeline pipeline) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.update(pipeline);
            transaction.commit();
            return pipeline;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error updating pipeline: " + e.getMessage(), e);
        }
    }

    /**
     * Update pipeline status
     * @param id Pipeline ID
     * @param status New status
     * @param lastRun Last run timestamp
     */
    public void updateStatus(Integer id, String status, String lastRun) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Query<?> query = session.createQuery(
                "UPDATE Pipeline SET status = :status, lastRun = :lastRun WHERE id = :id");
            query.setParameter("status", status);
            query.setParameter("lastRun", lastRun);
            query.setParameter("id", id);
            query.executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error updating pipeline status: " + e.getMessage(), e);
        }
    }

    /**
     * Delete pipeline by ID
     * @param id Pipeline ID to delete
     * @return true if deleted
     */
    public boolean delete(Integer id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Pipeline pipeline = session.get(Pipeline.class, id);
            if (pipeline != null) {
                session.delete(pipeline);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error deleting pipeline: " + e.getMessage(), e);
        }
    }

    /**
     * Count pipelines by user
     * @param userId User ID
     * @return Count of pipelines
     */
    public Long countByUserId(Integer userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(p) FROM Pipeline p WHERE p.user.id = :userId", Long.class);
            query.setParameter("userId", userId);
            return query.uniqueResult();
        }
    }
}
