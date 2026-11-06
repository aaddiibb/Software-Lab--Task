package com.university.ums.controller;

import com.university.ums.dto.Dtos.StudentRequest;
import com.university.ums.dto.Dtos.StudentResponse;
import com.university.ums.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public List<StudentResponse> all() {
        return service.findAll();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public StudentResponse me(Authentication auth) {
        return service.findByUsername(auth.getName());
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public StudentResponse updateMe(Authentication auth, @Valid @RequestBody StudentRequest req) {
        return service.updateByUsername(auth.getName(), req);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER') or @guard.isSelfStudent(#id, authentication)")
    public StudentResponse one(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public StudentResponse create(@Valid @RequestBody StudentRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public StudentResponse update(@PathVariable Long id, @Valid @RequestBody StudentRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
