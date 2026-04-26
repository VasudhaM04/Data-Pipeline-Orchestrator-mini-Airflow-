package com.pipeline.service;

import com.pipeline.model.Dependency;
import com.pipeline.model.Pipeline;
import com.pipeline.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit Tests for DAG Engine
 * 
 * CORE ADVANCED JAVA CONCEPTS TESTED:
 * - Collections Framework (HashMap, HashSet, List, Queue, PriorityQueue)
 * - Graph Algorithms (Topological Sort, Cycle Detection)
 * 
 * These tests verify the DAG execution engine works correctly.
 */
class DagEngineTest {

    private Pipeline simplePipeline;
    private Pipeline complexPipeline;
    private Pipeline cyclicPipeline;

    @BeforeEach
    void setUp() {
        // Create simple linear pipeline: A -> B -> C
        simplePipeline = createSimplePipeline();
        
        // Create complex DAG pipeline
        complexPipeline = createComplexPipeline();
        
        // Create cyclic pipeline (invalid)
        cyclicPipeline = createCyclicPipeline();
    }

    @Test
    void testTopologicalSort_SimpleLinear() {
        DagEngine engine = new DagEngine(simplePipeline);
        
        Set<Task> tasks = new HashSet<>(simplePipeline.getTasks());
        List<Task> sorted = engine.topologicalSort(tasks);
        
        // Verify all tasks included
        assertEquals(3, sorted.size(), "Should have 3 tasks in sorted order");
        
        // Verify order: Task A should come before Task B and C
        int indexA = findTaskIndex(sorted, "Task A");
        int indexB = findTaskIndex(sorted, "Task B");
        int indexC = findTaskIndex(sorted, "Task C");
        
        assertTrue(indexA < indexB, "Task A should come before Task B");
        assertTrue(indexB < indexC, "Task B should come before Task C");
    }

    @Test
    void testTopologicalSort_ComplexDAG() {
        DagEngine engine = new DagEngine(complexPipeline);
        
        Set<Task> tasks = new HashSet<>(complexPipeline.getTasks());
        List<Task> sorted = engine.topologicalSort(tasks);
        
        assertEquals(4, sorted.size(), "Should have 4 tasks in sorted order");
        
        // Verify dependencies are respected
        // Graph: A -> C, B -> C, C -> D
        // So A and B should come before C, and C before D
        int indexA = findTaskIndex(sorted, "Fetch");
        int indexB = findTaskIndex(sorted, "Clean");
        int indexC = findTaskIndex(sorted, "Transform");
        int indexD = findTaskIndex(sorted, "Load");
        
        assertTrue(indexA < indexC, "Fetch should come before Transform");
        assertTrue(indexB < indexC, "Clean should come before Transform");
        assertTrue(indexC < indexD, "Transform should come before Load");
    }

    @Test
    void testCycleDetection_NoCycle() {
        DagEngine engine = new DagEngine(simplePipeline);
        assertFalse(engine.hasCycle(), "Simple linear pipeline should not have cycle");
    }

    @Test
    void testCycleDetection_WithCycle() {
        DagEngine engine = new DagEngine(cyclicPipeline);
        assertTrue(engine.hasCycle(), "Cyclic pipeline should have cycle");
    }

    @Test
    void testGetInitialReadyTasks() {
        DagEngine engine = new DagEngine(complexPipeline);
        
        Set<Task> allTasks = new HashSet<>(complexPipeline.getTasks());
        Set<Task> readyTasks = engine.getInitialReadyTasks(allTasks);
        
        // Root tasks (Fetch and Clean) have no dependencies
        assertEquals(2, readyTasks.size(), "Should have 2 initially ready tasks");
        
        for (Task task : readyTasks) {
            assertTrue(
                task.getName().equals("Fetch") || task.getName().equals("Clean"),
                "Ready tasks should be Fetch or Clean"
            );
        }
    }

    @Test
    void testDependencySatisfaction() {
        DagEngine engine = new DagEngine(simplePipeline);
        Set<Task> allTasks = new HashSet<>(simplePipeline.getTasks());
        
        // Initially, only Task A should have satisfied dependencies
        Task taskA = findTaskByName(allTasks, "Task A");
        Task taskB = findTaskByName(allTasks, "Task B");
        Task taskC = findTaskByName(allTasks, "Task C");
        
        assertTrue(engine.areDependenciesSatisfied(taskA), "Task A has no dependencies");
        assertFalse(engine.areDependenciesSatisfied(taskB), "Task B depends on A");
        assertFalse(engine.areDependenciesSatisfied(taskC), "Task C depends on B");
        
        // Mark A as completed
        engine.markCompleted(taskA.getId());
        
        assertTrue(engine.areDependenciesSatisfied(taskB), "Task B dependencies should be satisfied after A completes");
        assertFalse(engine.areDependenciesSatisfied(taskC), "Task C still depends on B");
    }

    @Test
    void testGetExecutionLevels() {
        DagEngine engine = new DagEngine(complexPipeline);
        Set<Task> allTasks = new HashSet<>(complexPipeline.getTasks());
        
        Map<Integer, List<Task>> levels = engine.getExecutionLevels(allTasks);
        
        // Level 0: Fetch, Clean (no dependencies)
        assertTrue(levels.containsKey(0), "Should have level 0");
        assertEquals(2, levels.get(0).size(), "Level 0 should have 2 tasks");
        
        // Level 1: Transform (depends on Fetch and Clean)
        assertTrue(levels.containsKey(1), "Should have level 1");
        assertEquals(1, levels.get(1).size(), "Level 1 should have 1 task (Transform)");
        
        // Level 2: Load (depends on Transform)
        assertTrue(levels.containsKey(2), "Should have level 2");
        assertEquals(1, levels.get(2).size(), "Level 2 should have 1 task (Load)");
    }

    @Test
    void testOrderByPriority() {
        DagEngine engine = new DagEngine(simplePipeline);
        Set<Task> allTasks = new HashSet<>(simplePipeline.getTasks());
        
        List<Task> ordered = engine.orderByPriority(allTasks);
        
        // Verify ordering by priority (higher priority first)
        for (int i = 0; i < ordered.size() - 1; i++) {
            assertTrue(
                ordered.get(i).getPriority() >= ordered.get(i + 1).getPriority(),
                "Tasks should be ordered by priority descending"
            );
        }
    }

    @Test
    void testGraphStructure_AdjacencyList() {
        DagEngine engine = new DagEngine(complexPipeline);
        
        Map<Integer, List<Integer>> adjacencyList = engine.getAdjacencyList();
        
        // Fetch and Clean should have Transform as dependent
        Task fetch = findTaskByName(new HashSet<>(complexPipeline.getTasks()), "Fetch");
        Task clean = findTaskByName(new HashSet<>(complexPipeline.getTasks()), "Clean");
        Task transform = findTaskByName(new HashSet<>(complexPipeline.getTasks()), "Transform");
        
        assertTrue(adjacencyList.get(fetch.getId()).contains(transform.getId()),
            "Fetch should have Transform in adjacency list");
        assertTrue(adjacencyList.get(clean.getId()).contains(transform.getId()),
            "Clean should have Transform in adjacency list");
    }

    @Test
    void testCompletedTasksTracking() {
        DagEngine engine = new DagEngine(simplePipeline);
        
        assertEquals(0, engine.getCompletedCount(), "Initially no tasks completed");
        
        Set<Task> allTasks = new HashSet<>(simplePipeline.getTasks());
        Task taskA = findTaskByName(allTasks, "Task A");
        
        engine.markCompleted(taskA.getId());
        
        assertEquals(1, engine.getCompletedCount(), "One task should be completed");
        assertTrue(engine.isCompleted(taskA.getId()), "Task A should be marked as completed");
    }

    // Helper methods
    
    private Pipeline createSimplePipeline() {
        Pipeline pipeline = new Pipeline();
        pipeline.setId(1);
        pipeline.setName("Simple Pipeline");
        
        // Create tasks: A -> B -> C
        Task taskA = new Task("Task A", "CODE", "script=task_a.sh", 3);
        taskA.setId(1);
        
        Task taskB = new Task("Task B", "CODE", "script=task_b.sh", 2);
        taskB.setId(2);
        
        Task taskC = new Task("Task C", "CODE", "script=task_c.sh", 1);
        taskC.setId(3);
        
        // Create dependencies: B depends on A, C depends on B
        Dependency dep1 = new Dependency(taskB, taskA);
        taskB.addDependency(dep1);
        
        Dependency dep2 = new Dependency(taskC, taskB);
        taskC.addDependency(dep2);
        
        pipeline.addTask(taskA);
        pipeline.addTask(taskB);
        pipeline.addTask(taskC);
        
        return pipeline;
    }
    
    private Pipeline createComplexPipeline() {
        Pipeline pipeline = new Pipeline();
        pipeline.setId(2);
        pipeline.setName("Complex DAG Pipeline");
        
        // Create tasks: Fetch, Clean -> Transform -> Load
        Task fetch = new Task("Fetch", "API", "url=https://api.example.com", 1);
        fetch.setId(1);
        
        Task clean = new Task("Clean", "CODE", "script=clean.py", 1);
        clean.setId(2);
        
        Task transform = new Task("Transform", "CODE", "script=transform.py", 2);
        transform.setId(3);
        
        Task load = new Task("Load", "DATABASE", "query=INSERT INTO table", 1);
        load.setId(4);
        
        // Dependencies: Transform depends on Fetch and Clean, Load depends on Transform
        transform.addDependency(new Dependency(transform, fetch));
        transform.addDependency(new Dependency(transform, clean));
        load.addDependency(new Dependency(load, transform));
        
        pipeline.addTask(fetch);
        pipeline.addTask(clean);
        pipeline.addTask(transform);
        pipeline.addTask(load);
        
        return pipeline;
    }
    
    private Pipeline createCyclicPipeline() {
        Pipeline pipeline = new Pipeline();
        pipeline.setId(3);
        pipeline.setName("Cyclic Pipeline");
        
        // Create tasks with cycle: A -> B -> C -> A
        Task taskA = new Task("Task A", "CODE", "", 1);
        taskA.setId(1);
        
        Task taskB = new Task("Task B", "CODE", "", 1);
        taskB.setId(2);
        
        Task taskC = new Task("Task C", "CODE", "", 1);
        taskC.setId(3);
        
        // Create cycle: A -> B -> C -> A
        taskB.addDependency(new Dependency(taskB, taskA));
        taskC.addDependency(new Dependency(taskC, taskB));
        taskA.addDependency(new Dependency(taskA, taskC)); // This creates the cycle
        
        pipeline.addTask(taskA);
        pipeline.addTask(taskB);
        pipeline.addTask(taskC);
        
        return pipeline;
    }
    
    private int findTaskIndex(List<Task> tasks, String name) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }
    
    private Task findTaskByName(Set<Task> tasks, String name) {
        for (Task task : tasks) {
            if (task.getName().equals(name)) {
                return task;
            }
        }
        return null;
    }
}
