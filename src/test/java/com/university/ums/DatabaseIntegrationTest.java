package com.university.ums;

import com.university.ums.model.Course;
import com.university.ums.model.Role;
import com.university.ums.repository.AppUserRepository;
import com.university.ums.repository.CourseRepository;
import com.university.ums.repository.StudentRepository;
import com.university.ums.repository.TeacherRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:integration-tests;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.seed-demo-data=false"
})
@AutoConfigureMockMvc
@Transactional
class DatabaseIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private StudentRepository students;

    @Autowired
    private TeacherRepository teachers;

    @Autowired
    private CourseRepository courses;

    @Autowired
    private AppUserRepository users;

    @Test
    void creatingStudentThroughApiPersistsProfileAndLoginAccount() throws Exception {
        mvc.perform(post("/api/students")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Grace Hopper","email":"grace@uni.edu","department":"CSE",
                                 "username":"grace","password":"grace123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("grace"));

        var student = students.findByUserUsername("grace").orElseThrow();
        var account = users.findByUsername("grace").orElseThrow();
        assertEquals("Grace Hopper", student.getName());
        assertEquals("grace@uni.edu", student.getEmail());
        assertEquals(Role.STUDENT, account.getRole());
        assertTrue(account.getPassword().startsWith("$2"), "password must be stored as a BCrypt hash");
    }

    @Test
    void courseEnrollmentThroughApiIsStoredInEnrollmentTable() throws Exception {
        long teacherId = createTeacher("teacher.db", "teacher.db@uni.edu");
        long studentId = createStudent("student.db", "student.db@uni.edu");

        mvc.perform(post("/api/courses")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"CSE220","title":"Database Systems","credits":3,"teacherId":%d}
                                """.formatted(teacherId)))
                .andExpect(status().isCreated());

        Course course = courses.findAll().stream()
                .filter(candidate -> candidate.getCode().equals("CSE220"))
                .findFirst().orElseThrow();
        mvc.perform(post("/api/courses/{id}/students/{studentId}", course.getId(), studentId)
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.students[0].id").value(studentId));

        Course persistedCourse = courses.findById(course.getId()).orElseThrow();
        assertEquals(teacherId, persistedCourse.getTeacher().getId());
        assertTrue(persistedCourse.getStudents().stream().anyMatch(s -> s.getId().equals(studentId)));
    }

    @Test
    void updatingAndDeletingStudentChangesDatabaseRecords() throws Exception {
        long studentId = createStudent("before", "before@uni.edu");

        mvc.perform(put("/api/students/{id}", studentId)
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Updated Student","email":"after@uni.edu","department":"EEE",
                                 "username":"after","password":"newpass"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.department").value("EEE"));

        assertEquals("Updated Student", students.findById(studentId).orElseThrow().getName());
        assertTrue(users.findByUsername("after").isPresent());

        mvc.perform(delete("/api/students/{id}", studentId)
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isNoContent());

        students.flush();
        assertFalse(students.findById(studentId).isPresent());
        assertFalse(users.findByUsername("after").isPresent());
    }

    private long createStudent(String username, String email) throws Exception {
        mvc.perform(post("/api/students")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Database Student","email":"%s","department":"CSE",
                                 "username":"%s","password":"pass123"}
                                """.formatted(email, username)))
                .andExpect(status().isCreated());
        return students.findByUserUsername(username).orElseThrow().getId();
    }

    private long createTeacher(String username, String email) throws Exception {
        mvc.perform(post("/api/teachers")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Database Teacher","email":"%s","department":"CSE",
                                 "designation":"Professor","username":"%s","password":"pass123"}
                                """.formatted(email, username)))
                .andExpect(status().isCreated());
        return teachers.findByUserUsername(username).orElseThrow().getId();
    }
}
