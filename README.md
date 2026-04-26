# Data Pipeline Orchestrator

**Smart Data Pipeline Orchestrator (Java Web Application)**

A workflow engine that models data pipelines as Directed Acyclic Graphs (DAGs) with automatic scheduling and dependency resolution.

---

## Advanced Java Concepts Demonstrated

### 1. Collections Framework (CORE ENGINE)
- **HashMap**: Adjacency list representation for DAG
- **HashSet**: Tracking completed tasks and dependencies
- **LinkedList**: Ready task queue for execution
- **PriorityQueue**: Priority-based task scheduling
- **ArrayList**: Task storage and graph traversal

### 2. String Handling
- Config parsing with `split()`, `trim()`
- Time format parsing (HH:MM)
- Log message formatting
- URL/Query parameter processing

### 3. JDBC (Low-level Database Control)
- `Connection`, `PreparedStatement`, `ResultSet`
- Execution logging with fine-grained SQL control
- Transaction management

### 4. Hibernate ORM (Object-Relational Mapping)
- Entity mapping with annotations
- Session factory pattern
- Lazy/eager loading
- Cascade operations

### 5. Servlets (MVC Controller)
- `@WebServlet` annotations
- `doGet()`, `doPost()` handling
- Request/Response processing

### 6. JSP (View Layer)
- JSTL (`<c:forEach>`, `<c:if>`, `<c:choose>`)
- Expression Language
- Form handling

### 7. Session Tracking
- `HttpSession` for login persistence
- Session attributes for user data
- Session timeout configuration

### 8. Multithreading & Scheduling
- `ExecutorService` for task execution
- `ScheduledExecutorService` for periodic scheduling
- `Future` for async result handling
- Thread pool management

### 9. Graph Algorithms
- Topological Sort (Kahn's Algorithm)
- Cycle Detection (DFS with 3-color marking)
- Dependency resolution
- Execution level calculation

### 10. JUnit Testing
- Unit tests for DAG operations
- String handling tests
- Executor tests

---

## Architecture

```
JSP (View Layer)
    ↓
Servlets (Controller Layer)
    ↓
Service Layer (Business Logic + DAG Engine + Scheduler)
    ↓
DAO Layer (Hibernate ORM + JDBC)
    ↓
Database (MySQL)
```

---

## Project Structure

```
data-pipeline-orchestrator/
├── pom.xml                           # Maven configuration
├── database_schema.sql               # MySQL database schema
├── src/
│   ├── main/
│   │   ├── java/com/pipeline/
│   │   │   ├── model/               # Hibernate Entities
│   │   │   │   ├── User.java
│   │   │   │   ├── Pipeline.java
│   │   │   │   ├── Task.java
│   │   │   │   ├── Dependency.java
│   │   │   │   └── ExecutionLog.java
│   │   │   ├── dao/                 # Data Access Objects
│   │   │   │   ├── UserDao.java
│   │   │   │   ├── PipelineDao.java
│   │   │   │   ├── TaskDao.java
│   │   │   │   ├── DependencyDao.java
│   │   │   │   └── ExecutionLogDao.java
│   │   │   ├── service/             # Business Logic
│   │   │   │   ├── DagEngine.java      # DAG execution engine
│   │   │   │   ├── PipelineExecutor.java
│   │   │   │   └── PipelineScheduler.java
│   │   │   ├── servlet/             # Controllers
│   │   │   │   ├── AuthServlet.java
│   │   │   │   ├── DashboardServlet.java
│   │   │   │   ├── PipelineServlet.java
│   │   │   │   └── SchedulerServlet.java
│   │   │   └── util/                # Utilities
│   │   │       ├── HibernateUtil.java
│   │   │       └── DatabaseConnection.java
│   │   ├── resources/
│   │   │   └── hibernate.cfg.xml    # Hibernate configuration
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   └── web.xml
│   │       ├── index.jsp
│   │       ├── login.jsp
│   │       ├── register.jsp
│   │       ├── dashboard.jsp
│   │       ├── create-pipeline.jsp
│   │       ├── view-pipeline.jsp
│   │       └── edit-pipeline.jsp
│   └── test/java/com/pipeline/
│       ├── service/DagEngineTest.java
│       ├── service/PipelineExecutorTest.java
│       └── util/StringHandlingTest.java
```

---

## Prerequisites

1. **Java 11 or higher**
2. **Apache Maven 3.6+**
3. **Apache Tomcat 9+**
4. **MySQL 8.0+**

---

## Setup Instructions

### 1. Database Setup

```sql
-- Create database
CREATE DATABASE pipeline_db;

-- Run the schema
-- Use MySQL Workbench or command line:
mysql -u root -p pipeline_db < database_schema.sql
```

Update database credentials in:
- `src/main/resources/hibernate.cfg.xml`
- `src/main/java/com/pipeline/util/DatabaseConnection.java`

### 2. Build Project

```bash
# Navigate to project directory
cd data-pipeline-orchestrator

# Build with Maven
mvn clean package
```

### 3. Deploy to Tomcat

```bash
# Copy WAR file to Tomcat webapps
cp target/pipeline-orchestrator.war $CATALINA_HOME/webapps/

# Or use IDE deployment (IntelliJ/Eclipse)
```

### 4. Access Application

```
http://localhost:8080/pipeline-orchestrator/
```

---

## Usage Guide

### 1. Register/Login
- Create a new account or login with existing credentials
- Session persists for 30 minutes

### 2. Create Pipeline
- Click "Create Pipeline"
- Enter name, description, and optional schedule (HH:MM format)
- Add tasks with:
  - Name and type (API, DATABASE, CODE, TRANSFORM, LOAD)
  - Priority (higher = runs first)
  - Configuration (format: `key=value;key2=value2`)
  - Dependencies (task indices, comma-separated)

### 3. Pipeline Execution
- **Manual**: Click "Run" from dashboard
- **Scheduled**: Set schedule time, scheduler automatically runs at specified time

### 4. Monitor Execution
- View task DAG structure
- Check execution status
- View execution history

---

## Key Features

### DAG-Based Execution
- Tasks execute respecting dependencies
- Cycle detection prevents invalid pipelines
- Parallel execution where possible

### Automatic Scheduling
- Daily schedule support (HH:MM format)
- Scheduler runs as background service
- Manual trigger option

### Priority-Based Execution
- Higher priority tasks execute first
- Configurable per task

### Dual Database Access
- Hibernate for ORM (entities, relationships)
- JDBC for execution logging (fine-grained control)

---

## Running Tests

```bash
mvn test
```

Test coverage includes:
- DAG topological sorting
- Cycle detection
- String config parsing
- Schedule validation
- Task priority ordering

---

## Module Mapping (For Viva)

| Module | Implementation |
|--------|---------------|
| **String Handling** | Config parsing in `PipelineExecutor.parseConfig()`, schedule validation |
| **Collections** | `DagEngine.java` - HashMap, HashSet, Queue, PriorityQueue, List for graph operations |
| **JDBC** | `ExecutionLogDao.java` - PreparedStatement, Connection for execution logs |
| **Servlets** | All `*Servlet.java` files - `@WebServlet`, session handling |
| **JSP** | All `*.jsp` files - JSTL, EL, forms |
| **Hibernate** | All model classes - `@Entity`, `@Table`, relationships |
| **Multithreading** | `PipelineExecutor.java`, `PipelineScheduler.java` - ExecutorService, ScheduledExecutorService |
| **JUnit** | All `*Test.java` files - Unit testing |
| **Session Tracking** | `AuthServlet.java` - HttpSession for login |

---

## Sample Pipeline Configuration

### ETL Pipeline Example

**Pipeline**: Daily Data Import
**Schedule**: 02:00

**Tasks**:
1. **Fetch** (API) - No dependencies
   - Config: `type=API;url=https://api.data.com/fetch;method=GET`
   
2. **Clean** (CODE) - No dependencies
   - Config: `type=CODE;script=clean_data.py`
   
3. **Transform** (TRANSFORM) - Depends on Fetch, Clean
   - Config: `type=TRANSFORM;rules=normalize`
   
4. **Load** (DATABASE) - Depends on Transform
   - Config: `type=DATABASE;query=INSERT INTO warehouse`

**Execution Order**:
```
Fetch ───┐
         ├─→ Transform ──→ Load
Clean ───┘
```

---

## Viva Questions & Answers

**Q: How does the Collections framework help in this project?**
A: We use HashMap for adjacency lists in DAG representation, HashSet for tracking completed tasks, LinkedList for ready queue, and PriorityQueue for priority-based scheduling.

**Q: Why use both Hibernate and JDBC?**
A: Hibernate provides ORM convenience for entities with relationships. JDBC gives fine-grained control needed for execution logging (high-frequency inserts, raw SQL).

**Q: How does the scheduler work?**
A: ScheduledExecutorService runs every minute to check for pipelines with matching schedule times, then triggers PipelineExecutor which uses a thread pool.

**Q: How is cycle detection implemented?**
A: DFS with 3-color marking (WHITE=unvisited, GRAY=visiting, BLACK=visited). If we encounter a GRAY node, a cycle exists.

**Q: How does session tracking work?**
A: HttpSession stores user object after login. Session timeout is 30 minutes. Servlets check session before processing requests.

---

## Troubleshooting

### Tomcat Deployment Issues
- Ensure `pom.xml` packaging is `war`
- Check Tomcat manager for deployment status

### Database Connection Issues
- Verify MySQL is running
- Check credentials in hibernate.cfg.xml
- Ensure database schema is created

### Hibernate Issues
- Check entity annotations
- Verify hibernate.cfg.xml mapping entries
- Enable SQL logging: `<property name="show_sql">true</property>`

### Scheduler Not Running
- Check SchedulerServlet is loaded (check logs)
- Verify scheduler.start() is called
- Check database for scheduled pipelines

---

## Author

**Advanced Java Mini Project**

---

## License

This project is for educational purposes.

