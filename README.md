# 🎓 College Event Management System

![Java](https://img.shields.io/badge/Java-11-orange?style=for-the-badge&logo=java)
![Maven](https://img.shields.io/badge/Maven-3.8.6-C71A36?style=for-the-badge&logo=apache-maven)
![Jenkins](https://img.shields.io/badge/Jenkins-CI%2FCD-D24939?style=for-the-badge&logo=jenkins)
![SQLite](https://img.shields.io/badge/SQLite-3.42-003B57?style=for-the-badge&logo=sqlite)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

> A lightweight, full-stack **College Event Management System** built with embedded Jetty + SQLite. Manage events, students, and registrations through a modern REST API and a sleek single-page UI — no Spring Boot, no external database server required.

---

## ✨ Features

| # | Feature | Endpoint |
|---|---------|----------|
| 1 | 📅 **Create Events** — name, date, venue & capacity | `POST /api/events` |
| 2 | 📋 **List All Events** — paginated event catalogue | `GET /api/events` |
| 3 | ✍️ **Register Students** — link students to events | `POST /api/registrations` |
| 4 | 👥 **View Participants** — per-event attendee list | `GET /api/registrations?eventId=X` |
| 5 | ❌ **Cancel Registration** — remove a booking | `DELETE /api/registrations/{id}` |
| 6 | 📊 **Capacity Tracking** — total / registered / remaining | `GET /api/events/{id}/capacity` |

---

## 🛠️ Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Java | 11 |
| Build Tool | Apache Maven | 3.8.6+ |
| HTTP Server | Eclipse Jetty (embedded) | 9.4.51 |
| Database | SQLite (xerial driver) | 3.42.0 |
| JSON | org.json | 20231013 |
| Servlet API | javax.servlet-api | 3.1.0 |
| Testing | JUnit | 4.13.2 |
| CI/CD | Jenkins Declarative Pipeline | 2.x |

---

## 🚀 Getting Started

### Prerequisites

- **Java 11+** — [Download JDK 11](https://adoptium.net/)
- **Maven 3.6+** — [Download Maven](https://maven.apache.org/download.cgi)
- Git

### Clone & Build

```bash
# 1. Clone the repository
git clone https://github.com/your-username/event-management.git
cd event-management

# 2. Build the shaded (fat) JAR
mvn clean package

# 3. Run the application
java -jar target/event-management-1.0.0-shaded.jar
```

### Access the Application

| Resource | URL |
|----------|-----|
| 🌐 Web UI | http://localhost:8080 |
| 📡 Events API | http://localhost:8080/api/events |
| 👥 Registrations API | http://localhost:8080/api/registrations |
| 📊 Dashboard API | http://localhost:8080/api/dashboard |

> **Note:** The SQLite database file `eventmanagement.db` is created automatically in the working directory on first run.

---

## 📡 API Endpoints

### Events

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|-------------|
| `GET` | `/api/events` | List all events | — |
| `POST` | `/api/events` | Create a new event | `{"name":"...", "date":"...", "venue":"...", "capacity":100}` |
| `GET` | `/api/events/{id}` | Get event by ID | — |
| `DELETE` | `/api/events/{id}` | Delete an event | — |
| `GET` | `/api/events/{id}/capacity` | Capacity stats | — |

### Students

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|-------------|
| `GET` | `/api/students` | List all students | — |
| `POST` | `/api/students` | Add a student | `{"name":"...", "email":"..."}` |

### Registrations

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|-------------|
| `POST` | `/api/registrations` | Register student for event | `{"studentName":"...", "email":"...", "eventId":1}` |
| `GET` | `/api/registrations?eventId=X` | View participants for event | — |
| `DELETE` | `/api/registrations/{id}` | Cancel a registration | — |

### Dashboard

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/dashboard` | Aggregated stats (events, students, registrations) |

---

## 🔧 Jenkins CI/CD Setup

### 1. Install Jenkins

```bash
# Download Jenkins WAR
wget https://get.jenkins.io/war-stable/latest/jenkins.war
java -jar jenkins.war
# Access at http://localhost:8080 (use a different port if conflict)
```

Or use the [Windows installer](https://www.jenkins.io/download/).

### 2. Configure Tools in Jenkins

Go to **Manage Jenkins → Tools** and add:

| Tool | Name (must match Jenkinsfile) | Version |
|------|-------------------------------|---------|
| JDK | `JDK 11` | Java 11 (Adoptium) |
| Maven | `Maven 3.8.6` | 3.8.6 |

### 3. Create a Pipeline Job

1. Click **New Item** → Enter name → Select **Pipeline** → OK
2. Under **Pipeline**, set **Definition** = `Pipeline script from SCM`
3. Set **SCM** = `Git` and enter your repository URL
4. Set **Script Path** = `Jenkinsfile`
5. Save and click **Build Now**

### 4. Pipeline Stages Explained

```
Checkout ──► Build ──► Test ──► Package ──► Archive ──► Deploy
```

| Stage | What it does |
|-------|-------------|
| **Checkout** | Pulls latest code from SCM |
| **Build** | `mvn clean compile -DskipTests` — compiles all Java sources |
| **Test** | `mvn test` — runs JUnit tests, publishes results |
| **Package** | `mvn package -DskipTests` — creates the shaded JAR |
| **Archive Artifacts** | Saves `target/*.jar` to Jenkins build history |
| **Deploy** | Stops old process, starts new JAR (simulation / extend for real server) |

---

## 📁 Project Structure

```
C:\Event_management\
├── Jenkinsfile                          # Jenkins declarative pipeline
├── pom.xml                              # Maven build descriptor
├── README.md                            # This file
├── .gitignore                           # Git ignore rules
└── src/
    ├── main/
    │   ├── java/com/eventmgmt/
    │   │   ├── Main.java                # Embedded Jetty startup
    │   │   ├── DatabaseManager.java     # SQLite singleton (CRUD)
    │   │   ├── model/
    │   │   │   ├── Event.java
    │   │   │   ├── Student.java
    │   │   │   └── Registration.java
    │   │   └── servlet/
    │   │       ├── EventServlet.java
    │   │       ├── StudentServlet.java
    │   │       ├── RegistrationServlet.java
    │   │       └── DashboardServlet.java
    │   └── webapp/
    │       └── index.html               # Single-page UI (embedded CSS + JS)
    └── test/
        └── java/com/eventmgmt/
            └── AppTest.java             # JUnit smoke tests
```

---

## 📸 Screenshots

> _Screenshots will be added after the first successful run._

| Dashboard | Events | Registrations |
|-----------|--------|---------------|
| _(coming soon)_ | _(coming soon)_ | _(coming soon)_ |

---

## 👨‍💻 Team / Author

| Role | Name |
|------|------|
| Backend Developer | _(Your Name)_ |
| Frontend Developer | _(Your Name)_ |
| DevOps / CI-CD | _(Your Name)_ |
| Database Design | _(Your Name)_ |

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

<p align="center">Built with ❤️ for College Event Management · Java 11 · Jetty · SQLite</p>
