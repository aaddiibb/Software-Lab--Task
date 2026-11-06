package com.university.ums.service;

import com.university.ums.exception.BadRequestException;
import com.university.ums.model.AppUser;
import com.university.ums.model.Role;
import com.university.ums.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AppUserRepository users;

    @Mock
    private PasswordEncoder encoder;

    @Test
    void newAccountEncodesPasswordAndAssignsRole() {
        when(encoder.encode("secret")).thenReturn("encoded-secret");
        AccountService service = new AccountService(users, encoder);

        AppUser account = service.newAccount("alice", "secret", Role.STUDENT);

        assertEquals("alice", account.getUsername());
        assertEquals("encoded-secret", account.getPassword());
        assertEquals(Role.STUDENT, account.getRole());
        verify(users).existsByUsername("alice");
        verify(encoder).encode("secret");
    }

    @Test
    void newAccountRejectsBlankPassword() {
        AccountService service = new AccountService(users, encoder);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> service.newAccount("alice", "  ", Role.STUDENT));

        assertEquals("password is required", exception.getMessage());
        verify(users, never()).existsByUsername("alice");
        verify(encoder, never()).encode("  ");
    }

    @Test
    void newAccountRejectsDuplicateUsername() {
        when(users.existsByUsername("alice")).thenReturn(true);
        AccountService service = new AccountService(users, encoder);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> service.newAccount("alice", "secret", Role.STUDENT));

        assertEquals("username 'alice' is already taken", exception.getMessage());
        verify(encoder, never()).encode("secret");
    }

    @Test
    void updateAccountKeepsPasswordWhenNewPasswordIsBlank() {
        AppUser account = new AppUser("old-name", "existing-hash", Role.TEACHER);
        AccountService service = new AccountService(users, encoder);

        service.updateAccount(account, "new-name", " ");

        assertEquals("new-name", account.getUsername());
        assertEquals("existing-hash", account.getPassword());
        assertSame(Role.TEACHER, account.getRole());
        verify(users).existsByUsername("new-name");
        verify(encoder, never()).encode(" ");
    }
}
