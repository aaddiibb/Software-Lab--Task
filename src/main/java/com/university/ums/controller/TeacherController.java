package com.university.ums.controller;

import com.university.ums.dto.Dtos.TeacherRequest;
import com.university.ums.dto.Dtos.TeacherResponse;
import com.university.ums.service.TeacherService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherService service;

    public TeacherController(TeacherService service) {
        this.service = service;
    }

    @GetMapping
    public List<TeacherResponse> all() {
        return service.findAll();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('TEACHER')")
    public TeacherResponse me(Authentication auth) {
        return service.findByUsername(auth.getName());
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('TEACHER')")
    public TeacherResponse updateMe(Authentication auth, @Valid @RequestBody TeacherRequest req) {
        return service.updateByUsername(auth.getName(), req);
    }

    @GetMapping("/{id}")
    public TeacherResponse one(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public TeacherResponse create(@Valid @RequestBody TeacherRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TeacherResponse update(@PathVariable Long id, @Valid @RequestBody TeacherRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
