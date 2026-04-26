package com.pipeline.model;

import javax.persistence.*;

/**
 * Dependency Entity - Hibernate ORM
 * Represents task dependencies for DAG structure
 * Module: Hibernate (JPA) + Collections Framework
 */
@Entity
@Table(name = "dependencies")
public class Dependency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depends_on")
    private Task dependsOn;

    @Column(name = "created_at")
    private String createdAt;

    // Default Constructor
    public Dependency() {}

    // Parameterized Constructor
    public Dependency(Task task, Task dependsOn) {
        this.task = task;
        this.dependsOn = dependsOn;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Task getDependsOn() {
        return dependsOn;
    }

    public void setDependsOn(Task dependsOn) {
        this.dependsOn = dependsOn;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Dependency{id=" + id + ", task=" + (task != null ? task.getId() : "null") + 
               ", dependsOn=" + (dependsOn != null ? dependsOn.getId() : "null") + "}";
    }
}
