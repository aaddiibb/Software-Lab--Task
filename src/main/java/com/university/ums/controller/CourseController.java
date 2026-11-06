package com.university.ums.controller;

import com.university.ums.dto.Dtos.CourseRequest;
import com.university.ums.dto.Dtos.CourseResponse;
import com.university.ums.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService service;

    public CourseController(CourseService service) {
        this.service = service;
    }

    @GetMapping
    public List<CourseResponse> all() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public CourseResponse one(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public CourseResponse create(@Valid @RequestBody CourseRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT') or @guard.isCourseTeacher(#id, authentication)")
    public CourseResponse update(@PathVariable Long id, @Valid @RequestBody CourseRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // ----- Enrollment: admin or the course's teacher -----

    @PostMapping("/{id}/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or @guard.isCourseTeacher(#id, authentication)")
    public CourseResponse enroll(@PathVariable Long id, @PathVariable Long studentId) {
        return service.enroll(id, studentId);
    }

    @DeleteMapping("/{id}/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or @guard.isCourseTeacher(#id, authentication)")
    public CourseResponse unenroll(@PathVariable Long id, @PathVariable Long studentId) {
        return service.unenroll(id, studentId);
    }

    // ----- Self enrollment: logged-in student -----

    @PostMapping("/{id}/enroll")
    @PreAuthorize("hasRole('STUDENT')")
    public CourseResponse enrollSelf(@PathVariable Long id, Authentication auth) {
        return service.enrollSelf(id, auth.getName(), true);
    }

    @DeleteMapping("/{id}/enroll")
    @PreAuthorize("hasRole('STUDENT')")
    public CourseResponse dropSelf(@PathVariable Long id, Authentication auth) {
        return service.enrollSelf(id, auth.getName(), false);
    }
}
