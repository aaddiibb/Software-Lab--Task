package com.university.ums.dto;

import com.university.ums.model.Course;
import com.university.ums.model.Student;
import com.university.ums.model.Teacher;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Comparator;
import java.util.List;

/**
 * Request / response payloads for the REST API.
 * Entities are never exposed directly so passwords and lazy relations stay out of JSON.
 */
public final class Dtos {

    private Dtos() {
    }

    // ---------- Requests ----------

    /** password is required on create, optional on update (omit it to keep the current one). */
    public record StudentRequest(
            @NotBlank String name,
            @NotBlank @Email String email,
            String department,
            @NotBlank String username,
            @Size(min = 4, message = "must be at least 4 characters") String password) {
    }

    public record TeacherRequest(
            @NotBlank String name,
            @NotBlank @Email String email,
            String department,
            String designation,
            @NotBlank String username,
            @Size(min = 4, message = "must be at least 4 characters") String password) {
    }

    public record CourseRequest(
            @NotBlank String code,
            @NotBlank String title,
            @Min(0) int credits,
            Long teacherId) {
    }

    public record AdminRequest(
            @NotBlank String username,
            @Size(min = 4, message = "must be at least 4 characters") String password) {
    }

    // ---------- Responses ----------

    public record StudentResponse(Long id, String name, String email, String department,
                                  String username, List<String> courses) {
        public static StudentResponse from(Student s) {
            return new StudentResponse(s.getId(), s.getName(), s.getEmail(), s.getDepartment(),
                    s.getUser().getUsername(),
                    s.getCourses().stream().map(Course::getCode).sorted().toList());
        }
    }

    public record TeacherResponse(Long id, String name, String email, String department,
                                  String designation, String username, List<String> courses) {
        public static TeacherResponse from(Teacher t) {
            return new TeacherResponse(t.getId(), t.getName(), t.getEmail(), t.getDepartment(),
                    t.getDesignation(), t.getUser().getUsername(),
                    t.getCourses().stream().map(Course::getCode).sorted().toList());
        }
    }

    public record CourseResponse(Long id, String code, String title, int credits,
                                 Long teacherId, String teacherName, List<StudentSummary> students) {
        public static CourseResponse from(Course c) {
            Teacher t = c.getTeacher();
            return new CourseResponse(c.getId(), c.getCode(), c.getTitle(), c.getCredits(),
                    t == null ? null : t.getId(),
                    t == null ? null : t.getName(),
                    c.getStudents().stream()
                            .sorted(Comparator.comparing(Student::getId))
                            .map(s -> new StudentSummary(s.getId(), s.getName()))
                            .toList());
        }
    }

    public record StudentSummary(Long id, String name) {
    }

    public record AdminResponse(Long id, String username) {
    }

    public record CurrentUserResponse(String username, String role, Object profile) {
    }

    public record ErrorResponse(int status, String error, Object details) {
    }
}
