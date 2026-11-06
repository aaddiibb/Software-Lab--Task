# University Management System

[![Java CI](https://github.com/aaddiibb/Software-Lab--Task/actions/workflows/java-ci.yml/badge.svg)](https://github.com/aaddiibb/Software-Lab--Task/actions/workflows/java-ci.yml)

A simple REST API built with Spring Boot 3, Spring Data JPA, and Spring Security, packaged with Docker.

## Run it

**With Docker (app + PostgreSQL):**
```bash
docker compose up --build
```

**Without Docker (in-memory H2 database):**
```bash
mvn spring-boot:run
```

The API runs at `http://localhost:8080`.

## Authentication

The API uses **HTTP Basic** auth. Accounts are stored in the database and passwords are hashed with BCrypt.
Default accounts (created on first start):

| Role    | Username   | Password     |
|---------|------------|--------------|
| ADMIN   | `admin`    | `admin123`   |
| TEACHER | `teacher1` | `teacher123` |
| STUDENT | `student1` | `student123` |

To change the admin login, set `ADMIN_USERNAME` and `ADMIN_PASSWORD`. To skip the demo teacher, student, and course, set `SEED_DEMO_DATA=false`.

Creating a student or teacher also creates their login account (the `username` and `password` fields in the request).

## Endpoints and permissions

| Method | Endpoint                               | Who can call it                    |
|--------|----------------------------------------|------------------------------------|
| GET    | `/api/health`                          | anyone (no login)                  |
| GET    | `/api/auth/me`                         | any logged-in user                 |
| GET    | `/api/students`                        | ADMIN, TEACHER                     |
| GET    | `/api/students/{id}`                   | ADMIN, TEACHER, that student       |
| GET    | `/api/students/me`                     | STUDENT                            |
| POST / PUT / DELETE | `/api/students[/{id}]`    | ADMIN                              |
| GET    | `/api/teachers`, `/api/teachers/{id}`  | any logged-in user                 |
| GET    | `/api/teachers/me`                     | TEACHER                            |
| POST / PUT / DELETE | `/api/teachers[/{id}]`    | ADMIN                              |
| GET    | `/api/courses`, `/api/courses/{id}`    | any logged-in user                 |
| POST / DELETE | `/api/courses[/{id}]`           | ADMIN                              |
| PUT    | `/api/courses/{id}`                    | ADMIN, the course's teacher        |
| POST / DELETE | `/api/courses/{id}/students/{studentId}` | ADMIN, the course's teacher (enroll / remove a student) |
| POST / DELETE | `/api/courses/{id}/enroll`      | STUDENT (enroll / drop themselves) |
| CRUD   | `/api/admins[/{id}]`                   | ADMIN                              |

Status codes: `401` if you're not logged in, `403` if you don't have permission, `404` if the item doesn't exist, `400` for invalid input, `409` for duplicates.

## Examples

```bash
# Who am I?
curl -u admin:admin123 http://localhost:8080/api/auth/me

# Create a student
curl -u admin:admin123 -H "Content-Type: application/json" \
  -d '{"name":"Grace Hopper","email":"grace@uni.edu","department":"CSE","username":"grace","password":"grace123"}' \
  http://localhost:8080/api/students

# Update a student (leave out "password" to keep the current one)
curl -u admin:admin123 -X PUT -H "Content-Type: application/json" \
  -d '{"name":"Grace B. Hopper","email":"grace@uni.edu","department":"CSE","username":"grace"}' \
  http://localhost:8080/api/students/2

# Create a teacher
curl -u admin:admin123 -H "Content-Type: application/json" \
  -d '{"name":"Dr. Knuth","email":"knuth@uni.edu","department":"CSE","designation":"Professor","username":"knuth","password":"knuth123"}' \
  http://localhost:8080/api/teachers

# Create a course taught by teacher 1
curl -u admin:admin123 -H "Content-Type: application/json" \
  -d '{"code":"CSE201","title":"Data Structures","credits":3,"teacherId":1}' \
  http://localhost:8080/api/courses

# Student enrolls in course 2
curl -u grace:grace123 -X POST http://localhost:8080/api/courses/2/enroll

# Delete a student
curl -u admin:admin123 -X DELETE http://localhost:8080/api/students/2
```

## Project structure

```
src/main/java/com/university/ums
├── config/       DataInitializer (creates the default admin and demo data)
├── controller/   REST controllers (Student, Teacher, Course, Admin, Auth)
├── dto/          Request and response records + validation
├── exception/    Custom exceptions + global JSON error handler
├── model/        JPA entities (AppUser, Student, Teacher, Course, Role)
├── repository/   Spring Data repositories
├── security/     SecurityConfig (HTTP Basic, BCrypt) + AccessGuard (ownership checks)
└── service/      Business logic
```

Run the tests with `mvn test`. The suite contains 8 isolated service unit tests and 3 full
Spring integration tests. The integration tests use an H2 test database and verify both the
HTTP responses and the records persisted by JPA.

### GitHub Actions

The `Java CI` workflow runs automatically on pushes and pull requests to `main`. It shows
the two unit-test classes and the H2 database integration tests as separate jobs. Push runs
also show the first intentional failure demonstration.

To demonstrate failed tests without keeping broken assertions in the repository:

1. Open **Actions → Java CI → Run workflow**.
2. Select `one` or `two` for **Number of intentional AccountService test failures to display**.
3. Open the resulting `Expected failure demo` job(s). The deliberately failed Maven step is
   marked red, while the real unit and integration jobs remain passed.

The failure demonstrations modify assertions only in the temporary GitHub-hosted runner.
Their steps use `continue-on-error`, so the expected demonstrations do not make the genuine
CI result fail. Pull requests skip both demonstrations; push runs show one, and manual runs
can show either one or two.
