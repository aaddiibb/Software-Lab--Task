package com.university.ums.config;

import com.university.ums.dto.Dtos.CourseRequest;
import com.university.ums.dto.Dtos.StudentRequest;
import com.university.ums.dto.Dtos.TeacherRequest;
import com.university.ums.model.Role;
import com.university.ums.repository.AppUserRepository;
import com.university.ums.service.AccountService;
import com.university.ums.service.CourseService;
import com.university.ums.service.StudentService;
import com.university.ums.service.TeacherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Creates the first admin account (and optional demo data) on an empty database.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AppUserRepository users;
    private final AccountService accounts;
    private final TeacherService teachers;
    private final StudentService students;
    private final CourseService courses;

    @Value("${app.admin.username}")
    private String adminUsername;
    @Value("${app.admin.password}")
    private String adminPassword;
    @Value("${app.seed-demo-data}")
    private boolean seedDemoData;

    public DataInitializer(AppUserRepository users, AccountService accounts, TeacherService teachers,
                           StudentService students, CourseService courses) {
        this.users = users;
        this.accounts = accounts;
        this.teachers = teachers;
        this.students = students;
        this.courses = courses;
    }

    @Override
    public void run(String... args) {
        if (users.countByRole(Role.ADMIN) == 0) {
            users.save(accounts.newAccount(adminUsername, adminPassword, Role.ADMIN));
            log.info("Created default admin account '{}'", adminUsername);
        }

        if (seedDemoData && users.count() == 1) {
            var t = teachers.create(new TeacherRequest("Dr. Alan Turing", "turing@uni.edu",
                    "CSE", "Professor", "teacher1", "teacher123"));
            var s = students.create(new StudentRequest("Ada Lovelace", "ada@uni.edu",
                    "CSE", "student1", "student123"));
            var c = courses.create(new CourseRequest("CSE101", "Intro to Programming", 3, t.id()));
            courses.enroll(c.id(), s.id());
            log.info("Seeded demo data: teacher1/teacher123, student1/student123, course CSE101");
        }

        if (seedDemoData) {
            seedAdditionalTeachers();
            seedAdditionalStudents();
            seedAdditionalCourses();
        }
    }

    private void seedAdditionalTeachers() {
        var teacherSeeds = new String[][]{
                {"Dr. Katherine Johnson", "katherine@uni.edu", "MAT", "Professor", "teacher2"},
                {"Dr. Michael Faraday", "faraday@uni.edu", "PHY", "Associate Professor", "teacher3"},
                {"Dr. Rosalind Franklin", "rosalind@uni.edu", "BIO", "Professor", "teacher4"},
                {"Dr. Tim Berners-Lee", "tim@uni.edu", "CSE", "Professor", "teacher5"},
                {"Dr. Chien-Shiung Wu", "chien@uni.edu", "PHY", "Lecturer", "teacher6"}
        };
        for (var seed : teacherSeeds) {
            if (!users.existsByUsername(seed[4])) {
                teachers.create(new TeacherRequest(seed[0], seed[1], seed[2], seed[3], seed[4], "teacher123"));
            }
        }
    }

    private void seedAdditionalStudents() {
        var studentSeeds = new String[][]{
                {"Katherine Coleman", "katherine.student@uni.edu", "CSE", "student2"},
                {"James Maxwell", "james.student@uni.edu", "PHY", "student3"},
                {"Rosalind Evans", "rosalind.student@uni.edu", "BIO", "student4"},
                {"Timothy Lee", "tim.student@uni.edu", "CSE", "student5"},
                {"Chien Wu", "chien.student@uni.edu", "PHY", "student6"}
        };
        for (var seed : studentSeeds) {
            if (!users.existsByUsername(seed[3])) {
                students.create(new StudentRequest(seed[0], seed[1], seed[2], seed[3], "student123"));
            }
        }
    }

    private void seedAdditionalCourses() {
        var courseSeeds = new String[][]{
                {"CSE102", "Data Structures", "4", "teacher2"},
                {"CSE103", "Database Systems", "3", "teacher3"},
                {"CSE104", "Computer Networks", "3", "teacher4"},
                {"CSE105", "Web Engineering", "3", "teacher5"},
                {"CSE106", "Operating Systems", "4", "teacher6"}
        };
        for (var seed : courseSeeds) {
            if (!courses.existsByCode(seed[0])) {
                var teacher = teachers.findByUsername(seed[3]);
                courses.create(new CourseRequest(seed[0], seed[1], Integer.parseInt(seed[2]), teacher.id()));
            }
        }
    }
}
