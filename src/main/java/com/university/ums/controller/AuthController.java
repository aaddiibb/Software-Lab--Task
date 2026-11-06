package com.university.ums.controller;

import com.university.ums.dto.Dtos.CurrentUserResponse;
import com.university.ums.service.AccountService;
import com.university.ums.service.StudentService;
import com.university.ums.service.TeacherService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {

    private final StudentService students;
    private final TeacherService teachers;
    private final AccountService accounts;

    public AuthController(StudentService students, TeacherService teachers, AccountService accounts) {
        this.students = students;
        this.teachers = teachers;
        this.accounts = accounts;
    }

    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    /** Returns who is logged in (useful to test credentials). */
    @GetMapping("/api/auth/me")
    public CurrentUserResponse me(Authentication auth) {
        String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        Object profile = switch (role) {
            case "STUDENT" -> students.findByUsername(auth.getName());
            case "TEACHER" -> teachers.findByUsername(auth.getName());
            case "ADMIN" -> accounts.findAdminByUsername(auth.getName());
            default -> null;
        };
        return new CurrentUserResponse(auth.getName(), role, profile);
    }
}
