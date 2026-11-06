package com.university.ums.controller;

import com.university.ums.dto.Dtos.AdminRequest;
import com.university.ums.dto.Dtos.AdminResponse;
import com.university.ums.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admins")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AccountService service;

    public AdminController(AccountService service) {
        this.service = service;
    }

    @GetMapping
    public List<AdminResponse> all() {
        return service.findAllAdmins();
    }

    @GetMapping("/{id}")
    public AdminResponse one(@PathVariable Long id) {
        return service.findAdmin(id);
    }

    @GetMapping("/me")
    public AdminResponse me(Authentication auth) {
        return service.findAdminByUsername(auth.getName());
    }

    @PostMapping
    @PreAuthorize("denyAll()")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminResponse create(@Valid @RequestBody AdminRequest req) {
        return service.createAdmin(req);
    }

    @PutMapping("/{id}")
    public AdminResponse update(@PathVariable Long id, @Valid @RequestBody AdminRequest req) {
        return service.updateAdmin(id, req);
    }

    @PutMapping("/me")
    public AdminResponse updateMe(Authentication auth, @Valid @RequestBody AdminRequest req) {
        return service.updateAdminByUsername(auth.getName(), req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("denyAll()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.deleteAdmin(id);
    }
}
