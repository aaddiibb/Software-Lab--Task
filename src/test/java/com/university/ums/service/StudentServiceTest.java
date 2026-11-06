package com.university.ums.service;

import com.university.ums.dto.Dtos.StudentRequest;
import com.university.ums.dto.Dtos.StudentResponse;
import com.university.ums.exception.BadRequestException;
import com.university.ums.exception.NotFoundException;
import com.university.ums.model.AppUser;
import com.university.ums.model.Role;
import com.university.ums.model.Student;
import com.university.ums.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository students;

    @Mock
    private AccountService accounts;

    @Test
    void createStudentSavesProfileWithStudentAccount() {
        StudentRequest request = request("Ada", "ada@uni.edu", "ada");
        AppUser account = new AppUser("ada", "hash", Role.STUDENT);
        when(accounts.newAccount("ada", "pass123", Role.STUDENT)).thenReturn(account);
        when(students.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));
        StudentService service = new StudentService(students, accounts);

        StudentResponse response = service.create(request);

        assertEquals("Ada", response.name());
        assertEquals("ada@uni.edu", response.email());
        assertEquals("ada", response.username());
        verify(students).save(any(Student.class));
    }

    @Test
    void createStudentRejectsDuplicateEmailBeforeCreatingAccount() {
        StudentRequest request = request("Ada", "ada@uni.edu", "ada");
        when(students.existsByEmail("ada@uni.edu")).thenReturn(true);
        StudentService service = new StudentService(students, accounts);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> service.create(request));

        assertEquals("email 'ada@uni.edu' is already used", exception.getMessage());
        verify(accounts, never()).newAccount(any(), any(), any());
        verify(students, never()).save(any());
    }

    @Test
    void findByIdThrowsWhenStudentDoesNotExist() {
        when(students.findById(99L)).thenReturn(Optional.empty());
        StudentService service = new StudentService(students, accounts);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.findById(99L));

        assertEquals("Student 99 not found", exception.getMessage());
    }

    @Test
    void updateStudentRejectsEmailOwnedByAnotherStudent() {
        Student existing = new Student("Ada", "old@uni.edu", "CSE",
                new AppUser("ada", "hash", Role.STUDENT));
        when(students.findById(1L)).thenReturn(Optional.of(existing));
        when(students.existsByEmail("taken@uni.edu")).thenReturn(true);
        StudentService service = new StudentService(students, accounts);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> service.update(1L, request("Ada", "taken@uni.edu", "ada")));

        assertEquals("email 'taken@uni.edu' is already used", exception.getMessage());
        verify(accounts, never()).updateAccount(any(), any(), any());
    }

    private static StudentRequest request(String name, String email, String username) {
        return new StudentRequest(name, email, "CSE", username, "pass123");
    }
}
