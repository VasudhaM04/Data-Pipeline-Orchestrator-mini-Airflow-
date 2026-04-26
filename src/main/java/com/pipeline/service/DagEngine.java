package com.pipeline.service;

import com.pipeline.model.Dependency;
import com.pipeline.model.Pipeline;
import com.pipeline.model.Task;

import java.util.*;

/**
 * DAG (Directed Acyclic Graph) Execution Engine
 * 
 * CORE ADVANCED JAVA CONCEPTS DEMONSTRATED:
 * - Collections Framework: HashMap, HashSet, LinkedList, PriorityQueue
 * - Graph Algorithms: Topological Sort, Dependency Resolution
 * - Multithreading: Concurrent task execution
 * 
 * This is the heart of the pipeline orchestrator - it models pipelines
 * as DAGs and executes tasks respecting dependencies.
 */
public class DagEngine {

    // Collections Framework: Map for adjacency list representation of graph
    private Map<Integer, List<Integer>> adjacencyList;
    
    // Collections Framework: Map to track task dependencies
    private Map<Integer, Set<Integer>> dependencyMap;
    
    // Collections Framework: Set to track completed tasks
    private Set<Integer> completedTasks;
    
    // Collections Framework: Queue for ready tasks
    private Queue<Task> readyQueue;
    
    // Collections Framework: PriorityQueue for priority-based scheduling
    private PriorityQueue<Task> priorityQueue;

    /**
     * Initialize DAG Engine with a pipeline
     * @param pipeline Pipeline containing tasks and dependencies
     */
    public DagEngine(Pipeline pipeline) {
        this.adjacencyList = new HashMap<>();
        this.dependencyMap = new HashMap<>();
        this.completedTasks = new HashSet<>();
        this.readyQueue = new LinkedList<>();
        
        // PriorityQueue with custom comparator - sorts by priority (higher = first)
        this.priorityQueue = new PriorityQueue<>(
            (t1, t2) -> Integer.compare(t2.getPriority(), t1.getPriority())
        );
        
        buildGraph(pipeline);
    }

    /**
     * Build the dependency graph from pipeline tasks
     * Module: Collections Framework (HashMap, HashSet)
     * 
     * @param pipeline Pipeline to build graph from
     */
    private void buildGraph(Pipeline pipeline) {
        // Initialize adjacency list for all tasks
        for (Task task : pipeline.getTasks()) {
            adjacencyList.put(task.getId(), new ArrayList<>());
            dependencyMap.put(task.getId(), new HashSet<>());
        }
        
        // Build adjacency list and dependency map
        for (Task task : pipeline.getTasks()) {
            for (Dependency dep : task.getDependencies()) {
                Integer dependencyId = dep.getDependsOn().getId();
                Integer taskId = task.getId();
                
                // Add edge: dependency -> task
                adjacencyList.get(dependencyId).add(taskId);
                
                // Track that task depends on dependencyId
                dependencyMap.get(taskId).add(dependencyId);
            }
        }
    }

    /**
     * Perform topological sort using Kahn's Algorithm
     * Returns execution order respecting all dependencies
     * 
     * Module: Collections Framework + Graph Algorithms
     * 
     * @param tasks Set of tasks to sort
     * @return List of tasks in execution order
     */
    public List<Task> topologicalSort(Set<Task> tasks) {
        List<Task> sortedOrder = new ArrayList<>();
        
        // Map to count in-degrees (number of dependencies)
        Map<Integer, Integer> inDegree = new HashMap<>();
        
        // Calculate in-degrees for all tasks
        for (Task task : tasks) {
            Integer taskId = task.getId();
            inDegree.put(taskId, dependencyMap.getOrDefault(taskId, new HashSet<>()).size());
        }
        
        // Queue for tasks with no dependencies (in-degree = 0)
        Queue<Task> zeroInDegree = new LinkedList<>();
        
        // Initialize queue with tasks that have no dependencies
        for (Task task : tasks) {
            if (inDegree.getOrDefault(task.getId(), 0) == 0) {
                zeroInDegree.add(task);
            }
        }
        
        // Process tasks
        while (!zeroInDegree.isEmpty()) {
            // Collections: Poll from queue
            Task current = zeroInDegree.poll();
            sortedOrder.add(current);
            
            // Reduce in-degree for all neighbors
            for (Integer neighborId : adjacencyList.getOrDefault(current.getId(), new ArrayList<>())) {
                int newDegree = inDegree.get(neighborId) - 1;
                inDegree.put(neighborId, newDegree);
                
                // If all dependencies satisfied, add to queue
                if (newDegree == 0) {
                    Task neighbor = findTaskById(tasks, neighborId);
                    if (neighbor != null) {
                        zeroInDegree.add(neighbor);
                    }
                }
            }
        }
        
        // Check for cycles
        if (sortedOrder.size() != tasks.size()) {
            throw new RuntimeException("Cycle detected in pipeline dependencies!");
        }
        
        return sortedOrder;
    }

    /**
     * Get the initial set of tasks ready for execution
     * (tasks with no dependencies)
     * 
     * Module: Collections (Set, List)
     * 
     * @param tasks All tasks in pipeline
     * @return Set of ready tasks
     */
    public Set<Task> getInitialReadyTasks(Set<Task> tasks) {
        Set<Task> ready = new HashSet<>();
        
        for (Task task : tasks) {
            // Task is ready if it has no dependencies
            Set<Integer> deps = dependencyMap.getOrDefault(task.getId(), new HashSet<>());
            if (deps.isEmpty()) {
                ready.add(task);
            }
        }
        
        return ready;
    }

    /**
     * Get tasks that become ready after a task completes
     * Module: Collections (Set, Map traversal)
     * 
     * @param completedTaskId ID of completed task
     * @param allTasks All tasks in pipeline
     * @return Set of newly ready tasks
     */
    public Set<Task> getNewlyReadyTasks(Integer completedTaskId, Set<Task> allTasks) {
        Set<Task> newlyReady = new HashSet<>();
        
        // Get all tasks that depend on the completed task
        List<Integer> dependents = adjacencyList.getOrDefault(completedTaskId, new ArrayList<>());
        
        for (Integer dependentId : dependents) {
            // Check if all dependencies of this task are completed
            Set<Integer> deps = dependencyMap.getOrDefault(dependentId, new HashSet<>());
            
            // Collections: Set.containsAll() - check if all dependencies are completed
            if (completedTasks.containsAll(deps)) {
                Task task = findTaskById(allTasks, dependentId);
                if (task != null) {
                    newlyReady.add(task);
                }
            }
        }
        
        return newlyReady;
    }

    /**
     * Mark a task as completed
     * @param taskId Task ID
     */
    public void markCompleted(Integer taskId) {
        completedTasks.add(taskId);
    }

    /**
     * Check if a task is completed
     * @param taskId Task ID
     * @return true if completed
     */
    public boolean isCompleted(Integer taskId) {
        return completedTasks.contains(taskId);
    }

    /**
     * Check if all dependencies of a task are completed
     * @param task Task to check
     * @return true if all dependencies are satisfied
     */
    public boolean areDependenciesSatisfied(Task task) {
        Set<Integer> deps = dependencyMap.getOrDefault(task.getId(), new HashSet<>());
        return completedTasks.containsAll(deps);
    }

    /**
     * Get all tasks that depend on a specific task
     * @param taskId Task ID
     * @return List of dependent task IDs
     */
    public List<Integer> getDependents(Integer taskId) {
        return adjacencyList.getOrDefault(taskId, new ArrayList<>());
    }

    /**
     * Get dependencies of a task
     * @param taskId Task ID
     * @return Set of dependency task IDs
     */
    public Set<Integer> getDependencies(Integer taskId) {
        return dependencyMap.getOrDefault(taskId, new HashSet<>());
    }

    /**
     * Check if the graph has any cycles
     * Uses DFS with three-color marking
     * 
     * WHITE = unvisited, GRAY = visiting, BLACK = visited
     * 
     * @return true if cycle detected
     */
    public boolean hasCycle() {
        // Collections: Map for color marking
        Map<Integer, String> color = new HashMap<>();
        
        // Initialize all as WHITE
        for (Integer taskId : adjacencyList.keySet()) {
            color.put(taskId, "WHITE");
        }
        
        // DFS from each unvisited node
        for (Integer taskId : adjacencyList.keySet()) {
            if ("WHITE".equals(color.get(taskId))) {
                if (dfsHasCycle(taskId, color)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * DFS helper for cycle detection
     * @param nodeId Current node
     * @param color Color map
     * @return true if cycle found
     */
    private boolean dfsHasCycle(Integer nodeId, Map<Integer, String> color) {
        color.put(nodeId, "GRAY"); // Currently visiting
        
        for (Integer neighbor : adjacencyList.getOrDefault(nodeId, new ArrayList<>())) {
            if ("GRAY".equals(color.get(neighbor))) {
                // Found back edge - cycle!
                return true;
            }
            if ("WHITE".equals(color.get(neighbor))) {
                if (dfsHasCycle(neighbor, color)) {
                    return true;
                }
            }
        }
        
        color.put(nodeId, "BLACK"); // Finished visiting
        return false;
    }

    /**
     * Get execution levels - groups tasks by their dependency depth
     * Level 0 = tasks with no dependencies
     * Level 1 = tasks that depend only on level 0 tasks
     * etc.
     * 
     * Module: Collections (Map, List)
     * 
     * @param tasks All tasks
     * @return Map of level -> list of tasks
     */
    public Map<Integer, List<Task>> getExecutionLevels(Set<Task> tasks) {
        Map<Integer, List<Task>> levels = new HashMap<>();
        Map<Integer, Integer> taskLevel = new HashMap<>();
        
        // Initialize all tasks with level 0
        for (Task task : tasks) {
            taskLevel.put(task.getId(), 0);
        }
        
        // Calculate levels based on dependencies
        boolean changed;
        do {
            changed = false;
            for (Task task : tasks) {
                Integer taskId = task.getId();
                Set<Integer> deps = dependencyMap.getOrDefault(taskId, new HashSet<>());
                
                int maxDependencyLevel = 0;
                for (Integer depId : deps) {
                    int depLevel = taskLevel.getOrDefault(depId, 0);
                    maxDependencyLevel = Math.max(maxDependencyLevel, depLevel + 1);
                }
                
                if (maxDependencyLevel > taskLevel.get(taskId)) {
                    taskLevel.put(taskId, maxDependencyLevel);
                    changed = true;
                }
            }
        } while (changed);
        
        // Group by level
        for (Task task : tasks) {
            Integer level = taskLevel.get(task.getId());
            levels.computeIfAbsent(level, k -> new ArrayList<>()).add(task);
        }
        
        return levels;
    }

    /**
     * Priority-based task ordering
     * Higher priority tasks are executed first within their level
     * 
     * Module: PriorityQueue
     * 
     * @param tasks Tasks to order
     * @return List ordered by priority
     */
    public List<Task> orderByPriority(Set<Task> tasks) {
        PriorityQueue<Task> pq = new PriorityQueue<>(
            (t1, t2) -> Integer.compare(t2.getPriority(), t1.getPriority())
        );
        
        pq.addAll(tasks);
        
        List<Task> ordered = new ArrayList<>();
        while (!pq.isEmpty()) {
            ordered.add(pq.poll());
        }
        
        return ordered;
    }

    /**
     * Find task by ID from a set of tasks
     * @param tasks Set of tasks
     * @param taskId Task ID to find
     * @return Task or null
     */
    private Task findTaskById(Set<Task> tasks, Integer taskId) {
        for (Task task : tasks) {
            if (task.getId().equals(taskId)) {
                return task;
            }
        }
        return null;
    }

    /**
     * Get the adjacency list representation
     * @return Map of task ID to list of dependent task IDs
     */
    public Map<Integer, List<Integer>> getAdjacencyList() {
        return new HashMap<>(adjacencyList);
    }

    /**
     * Get the dependency map
     * @return Map of task ID to set of dependency task IDs
     */
    public Map<Integer, Set<Integer>> getDependencyMap() {
        Map<Integer, Set<Integer>> copy = new HashMap<>();
        for (Map.Entry<Integer, Set<Integer>> entry : dependencyMap.entrySet()) {
            copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return copy;
    }

    /**
     * Get count of completed tasks
     * @return Number of completed tasks
     */
    public int getCompletedCount() {
        return completedTasks.size();
    }

    /**
     * Reset the engine state
     */
    public void reset() {
        completedTasks.clear();
        readyQueue.clear();
        priorityQueue.clear();
    }
}
