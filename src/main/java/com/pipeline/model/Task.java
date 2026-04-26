package com.pipeline.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Task Entity - Hibernate ORM
 * Represents an individual task in a pipeline
 * Module: Hibernate (JPA)
 */
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "config")
    private String config;

    @Column(name = "priority")
    private Integer priority = 0;

    @Column(name = "status")
    private String status = "PENDING";

    @Column(name = "created_at")
    private String createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_id")
    private Pipeline pipeline;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Dependency> dependencies = new HashSet<>();

    @OneToMany(mappedBy = "dependsOn", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Dependency> dependents = new HashSet<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ExecutionLog> executionLogs = new HashSet<>();

    // Default Constructor
    public Task() {}

    // Parameterized Constructor
    public Task(String name, String type, String config, Integer priority) {
        this.name = name;
        this.type = type;
        this.config = config;
        this.priority = priority;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public Set<Dependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Set<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    public Set<Dependency> getDependents() {
        return dependents;
    }

    public void setDependents(Set<Dependency> dependents) {
        this.dependents = dependents;
    }

    public Set<ExecutionLog> getExecutionLogs() {
        return executionLogs;
    }

    public void setExecutionLogs(Set<ExecutionLog> executionLogs) {
        this.executionLogs = executionLogs;
    }

    // Helper method to add dependency
    public void addDependency(Dependency dependency) {
        dependencies.add(dependency);
        dependency.setTask(this);
    }

    // Helper method to check if task has dependencies
    public boolean hasDependencies() {
        return dependencies != null && !dependencies.isEmpty();
    }

    @Override
    public String toString() {
        return "Task{id=" + id + ", name='" + name + "', type='" + type + "', status='" + status + "'}";
    }
}
