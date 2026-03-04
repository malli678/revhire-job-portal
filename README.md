# RevHire - Job Portal Application

## 📖 Application Overview
RevHire is a full-stack monolithic web application that serves as a job portal connecting job seekers with employers. Job seekers can create profiles, build and upload resumes, search for jobs using advanced filters, and track their applications. Employers can post job openings, manage applications, and shortlist/reject candidates. The application features a responsive web interface, role-based access control, and real-time notifications for enhanced user experience.

---

## 👥 Team Members & Responsibilities

| Name | Role | Module | GitHub ID |
|------|------|--------|-----------|
| **Mandava Mallikarjun** | Project Lead | Authentication & Security | [@malli678](https://github.com/malli678) |
| **Bhukya Bhanu Prakash Naik** | Team Member | Job Seeker Profile & Resume | [@Bhanu5355](https://github.com/Bhanu5355) |
| **Sanikommu Bhavya Sri** | Team Member | Job Posting & Search | [@bhavyasri-cyber](https://github.com/bhavyasri-cyber) |
| **Shaik Mubeen** | Team Member | Application Module | [@MUBEENSHAIK1](https://github.com/MUBEENSHAIK1) |
| **Regula Venkata Dharani Kumar** | Team Member | Employer Profile & Dashboard | [@Dharanikumar098](https://github.com/Dharanikumar098) |
| **Katika Dhanunjaya** | Team Member | Notification & UI Module | [@dhanunjaya999](https://github.com/dhanunjaya999) |

---

## 🚀 Core Features

### As a Job Seeker
- **Account Management:** Register and login with secure JWT authentication.
- **Profile Building:** Manage personal details, education, experience, skills, and certifications.
- **Resume Capabilities:** Create a structured textual resume and upload formatted resumes (PDF/DOCX, max 2MB).
- **Job Discovery:** Search using robust filters (role, location, experience, salary range, job type). Save jobs for later.
- **Application Tracking:** Apply with one click, track application status, and optionally withdraw applications.
- **Notifications:** Receive in-app alerts for application status updates and job recommendations.

### As an Employer
- **Company Management:** Register and manage company profile, industry, size, and location.
- **Job Postings:** Create comprehensive job listings with detailed requirements, salary, and deadlines.
- **Application Management:** View applicant details, resumes, and cover letters. Shortlist or reject candidates with bulk actions and internal notes.
- **Candidate Filtering:** Filter incoming applicants by experience, skills, and application date.
- **Dashboard Analytics:** View statistics on total jobs, active jobs, and pending applications.

---

## 🛠️ Technology Stack
- **Backend Framework:** Java 17, Spring Boot
- **Frontend Template Engine:** Thymeleaf, HTML5, Vanilla CSS, JS
- **Security:** Spring Security (JWT-based), BCrypt Password Encryption
- **Database:** Oracle SQL Database / PostgreSQL
- **Testing:** JUnit4, Mockito
- **Tools:** Maven 3.8+, Git, Log4J, Apache Tika (Resume text extraction)

---

## 📁 Project Structure

```text
revhire/
├── src/
│   ├── main/
│   │   ├── java/com/revhire/
│   │   │   ├── config/       # Spring Security & App Configurations
│   │   │   ├── controller/   # REST & MVC Controllers (Auth, Job, JobSeeker, Employer)
│   │   │   ├── dto/          # Data Transfer Objects
│   │   │   ├── exception/    # Global Exception Handling
│   │   │   ├── model/        # Entities (User, JobSeeker, Employer, Job, Application)
│   │   │   ├── repository/   # Spring Data JPA Repositories
│   │   │   ├── service/      # Business Logic (User, Job, Application, Parsing)
│   │   │   └── RevhireApplication.java
│   │   └── resources/
│   │       ├── static/       # CSS, JS, Images
│   │       ├── templates/    # Thymeleaf HTML Templates
│   │       └── application.properties # DB & App Config
│   └── test/                 # Component & Integration Tests
└── pom.xml                   # Maven Dependencies
```

---

## 📊 Application Architecture & ERD

**Architecture Overview:**
RevHire follows a classic Monolithic MVC (Model-View-Controller) architecture utilizing Spring Boot. 
- **View Layer:** Thymeleaf server-side rendered HTML templates dynamically injected with data.
- **Controller Layer:** Spring MVC controllers handle HTTP requests and route payload data.
- **Service Layer:** Business-critical logic, resume parsing (Apache Tika), and transaction management.
- **Data Access Layer:** JPA/Hibernate securely interacting with the relational database.

**Entity Relationship Overview (ERD Focus):**
- `User` (Base class) -> Inherited by `JobSeeker` and `Employer`.
- `Employer` -> One-to-Many -> `Job`.
- `JobSeeker` -> One-to-Many -> `Application`, `SavedJob`, `Experience`, `Education`.
- `Job` -> One-to-Many -> `Application`.
- `User` -> One-to-Many -> `Notification`.

*(Note: Physical diagram artifacts represent the associations described above).*

---

## 🔧 Setup Instructions

### Prerequisites
- Java 17 or higher
- Oracle Database (or any SQL database supported by Hibernate)
- Maven 3.8+
- IDE (Eclipse / IntelliJ IDEA)

### Database Setup
1. Create a locally hosted database instance. For Oracle:
```sql
-- Create database user
CREATE USER revhire IDENTIFIED BY password;
GRANT CONNECT, RESOURCE TO revhire;
```
2. Update the `src/main/resources/application.properties` with your exact DB credentials:
```properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:XE
spring.datasource.username=revhire
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update
```

### Application Execution
1. Clone the repository.
2. Navigate to the project root directory.
3. Build the application using Maven:
   ```bash
   mvn clean install
   ```
4. Run the Spring Boot application:
   ```bash
   mvn spring-boot:run
   ```
5. Access the application at `http://localhost:8080/`.

---

## 🔑 Default Credentials (For Testing)

If pre-loaded data is configured:
- **Employer User:** `employer@revhire.com` / `password123`
- **Jobseeker User:** `jobseeker@revhire.com` / `password123`

---

## 🐞 Known Issues
- Large resume parsing operations may experience slight processing lag.
- Flash messages occasionally stack if rapidly navigating.

---

## 🔮 Future Enhancements
- **AI-Powered Matching:** Implementing advanced ranking algorithms to recommend candidates to employers automatically.
- **Real-time Chat:** Enabling direct messaging between shortlisted applicants and employers.
- **Interview Scheduling:** Internal calendar integration to book technical screening rounds.
