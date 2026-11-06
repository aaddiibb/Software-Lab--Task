package com.university.ums.service;

import com.university.ums.dto.Dtos.AdminRequest;
import com.university.ums.dto.Dtos.AdminResponse;
import com.university.ums.exception.BadRequestException;
import com.university.ums.exception.NotFoundException;
import com.university.ums.model.AppUser;
import com.university.ums.model.Role;
import com.university.ums.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Creates/updates login accounts and manages admin accounts.
 */
@Service
@Transactional
public class AccountService {

    private final AppUserRepository users;
    private final PasswordEncoder encoder;

    public AccountService(AppUserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    public AppUser newAccount(String username, String rawPassword, Role role) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new BadRequestException("password is required");
        }
        if (users.existsByUsername(username)) {
            throw new BadRequestException("username '" + username + "' is already taken");
        }
        return new AppUser(username, encoder.encode(rawPassword), role);
    }

    /** Changes username/password of an existing account; a blank password keeps the old one. */
    public void updateAccount(AppUser user, String username, String rawPassword) {
        if (!user.getUsername().equals(username)) {
            if (users.existsByUsername(username)) {
                throw new BadRequestException("username '" + username + "' is already taken");
            }
            user.setUsername(username);
        }
        if (rawPassword != null && !rawPassword.isBlank()) {
            user.setPassword(encoder.encode(rawPassword));
        }
    }

    // ---------- Admin CRUD ----------

    @Transactional(readOnly = true)
    public List<AdminResponse> findAllAdmins() {
        return users.findByRole(Role.ADMIN).stream().map(AccountService::toAdmin).toList();
    }

    @Transactional(readOnly = true)
    public AdminResponse findAdmin(Long id) {
        return toAdmin(getAdmin(id));
    }

    @Transactional(readOnly = true)
    public AdminResponse findAdminByUsername(String username) {
        return toAdmin(getAdmin(username));
    }

    public AdminResponse createAdmin(AdminRequest req) {
        return toAdmin(users.save(newAccount(req.username(), req.password(), Role.ADMIN)));
    }

    public AdminResponse updateAdmin(Long id, AdminRequest req) {
        AppUser admin = getAdmin(id);
        updateAccount(admin, req.username(), req.password());
        return toAdmin(admin);
    }

    /** Updates only the administrator account belonging to the authenticated username. */
    public AdminResponse updateAdminByUsername(String username, AdminRequest req) {
        AppUser admin = getAdmin(username);
        updateAccount(admin, req.username(), req.password());
        return toAdmin(admin);
    }

    public void deleteAdmin(Long id) {
        AppUser admin = getAdmin(id);
        if (users.countByRole(Role.ADMIN) <= 1) {
            throw new BadRequestException("cannot delete the last admin");
        }
        users.delete(admin);
    }

    private AppUser getAdmin(Long id) {
        return users.findById(id)
                .filter(u -> u.getRole() == Role.ADMIN)
                .orElseThrow(() -> new NotFoundException("Admin " + id + " not found"));
    }

    private AppUser getAdmin(String username) {
        return users.findByUsername(username)
                .filter(u -> u.getRole() == Role.ADMIN)
                .orElseThrow(() -> new NotFoundException("No admin profile for " + username));
    }

    private static AdminResponse toAdmin(AppUser u) {
        return new AdminResponse(u.getId(), u.getUsername());
    }
}
