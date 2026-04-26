package com.pipeline.dao;

import com.pipeline.model.Dependency;
import com.pipeline.model.Task;
import com.pipeline.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Dependency Data Access Object
 * Handles database operations for Dependency entity using Hibernate ORM
 * Module: Hibernate ORM + Collections (Set, List)
 */
public class DependencyDao {

    /**
     * Create a new dependency
     * @param dependency Dependency to create
     * @return Created dependency with ID
     */
    public Dependency create(Dependency dependency) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(dependency);
            transaction.commit();
            return dependency;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error creating dependency: " + e.getMessage(), e);
        }
    }

    /**
     * Create multiple dependencies in batch
     * @param dependencies Set of dependencies to create
     * @return Set of created dependencies
     */
    public Set<Dependency> createBatch(Set<Dependency> dependencies) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            for (Dependency dep : dependencies) {
                session.save(dep);
            }
            transaction.commit();
            return dependencies;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error creating dependencies: " + e.getMessage(), e);
        }
    }

    /**
     * Find dependency by ID
     * @param id Dependency ID
     * @return Dependency or null if not found
     */
    public Dependency findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Dependency.class, id);
        }
    }

    /**
     * Find all dependencies for a task
     * @param taskId Task ID
     * @return Set of dependencies
     */
    public Set<Dependency> findByTaskId(Integer taskId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Dependency> query = session.createQuery(
                "FROM Dependency WHERE task.id = :taskId", Dependency.class);
            query.setParameter("taskId", taskId);
            return new HashSet<>(query.list());
        }
    }

    /**
     * Find all dependencies for tasks in a pipeline
     * @param pipelineId Pipeline ID
     * @return List of dependencies
     */
    public List<Dependency> findByPipelineId(Integer pipelineId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Dependency> query = session.createQuery(
                "FROM Dependency WHERE task.pipeline.id = :pipelineId", 
                Dependency.class);
            query.setParameter("pipelineId", pipelineId);
            return query.list();
        }
    }

    /**
     * Find all dependencies
     * @return List of all dependencies
     */
    public List<Dependency> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Dependency", Dependency.class).list();
        }
    }

    /**
     * Delete dependency by ID
     * @param id Dependency ID to delete
     * @return true if deleted
     */
    public boolean delete(Integer id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Dependency dependency = session.get(Dependency.class, id);
            if (dependency != null) {
                session.delete(dependency);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error deleting dependency: " + e.getMessage(), e);
        }
    }

    /**
     * Delete all dependencies for a task
     * @param taskId Task ID
     * @return Number of deleted dependencies
     */
    public int deleteByTaskId(Integer taskId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Query<?> query = session.createQuery(
                "DELETE FROM Dependency WHERE task.id = :taskId");
            query.setParameter("taskId", taskId);
            int result = query.executeUpdate();
            transaction.commit();
            return result;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error deleting dependencies: " + e.getMessage(), e);
        }
    }

    /**
     * Check if a dependency exists between two tasks
     * @param taskId Task ID
     * @param dependsOnTaskId Dependency task ID
     * @return true if dependency exists
     */
    public boolean dependencyExists(Integer taskId, Integer dependsOnTaskId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(d) FROM Dependency d WHERE d.task.id = :taskId AND d.dependsOn.id = :dependsOnId", 
                Long.class);
            query.setParameter("taskId", taskId);
            query.setParameter("dependsOnId", dependsOnTaskId);
            return query.uniqueResult() > 0;
        }
    }

    /**
     * Check if task has any dependents
     * @param taskId Task ID
     * @return true if task has dependents
     */
    public boolean hasDependents(Integer taskId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(d) FROM Dependency d WHERE d.dependsOn.id = :taskId", Long.class);
            query.setParameter("taskId", taskId);
            return query.uniqueResult() > 0;
        }
    }

    /**
     * Get all tasks that depend on a given task
     * @param taskId Task ID
     * @return List of dependent tasks
     */
    public List<Task> getDependentTasks(Integer taskId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Task> query = session.createQuery(
                "SELECT d.task FROM Dependency d WHERE d.dependsOn.id = :taskId", Task.class);
            query.setParameter("taskId", taskId);
            return query.list();
        }
    }
}
