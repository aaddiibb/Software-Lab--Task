package com.university.ums.service;

import com.university.ums.dto.Dtos.StudentRequest;
import com.university.ums.dto.Dtos.StudentResponse;
import com.university.ums.exception.BadRequestException;
import com.university.ums.exception.NotFoundException;
import com.university.ums.model.Role;
import com.university.ums.model.Student;
import com.university.ums.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StudentService {

    private final StudentRepository students;
    private final AccountService accounts;

    public StudentService(StudentRepository students, AccountService accounts) {
        this.students = students;
        this.accounts = accounts;
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> findAll() {
        return students.findAll().stream().map(StudentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public StudentResponse findById(Long id) {
        return StudentResponse.from(get(id));
    }

    @Transactional(readOnly = true)
    public StudentResponse findByUsername(String username) {
        return students.findByUserUsername(username)
                .map(StudentResponse::from)
                .orElseThrow(() -> new NotFoundException("No student profile for " + username));
    }

    public StudentResponse create(StudentRequest req) {
        if (students.existsByEmail(req.email())) {
            throw new BadRequestException("email '" + req.email() + "' is already used");
        }
        Student s = new Student(req.name(), req.email(), req.department(),
                accounts.newAccount(req.username(), req.password(), Role.STUDENT));
        return StudentResponse.from(students.save(s));
    }

    public StudentResponse update(Long id, StudentRequest req) {
        Student s = get(id);
        return update(s, req);
    }

    /** Updates only the student profile belonging to the authenticated username. */
    public StudentResponse updateByUsername(String username, StudentRequest req) {
        Student s = students.findByUserUsername(username)
                .orElseThrow(() -> new NotFoundException("No student profile for " + username));
        return update(s, req);
    }

    private StudentResponse update(Student s, StudentRequest req) {
        if (!s.getEmail().equals(req.email()) && students.existsByEmail(req.email())) {
            throw new BadRequestException("email '" + req.email() + "' is already used");
        }
        s.setName(req.name());
        s.setEmail(req.email());
        s.setDepartment(req.department());
        accounts.updateAccount(s.getUser(), req.username(), req.password());
        return StudentResponse.from(s);
    }

    public void delete(Long id) {
        Student s = get(id);
        // Course owns the enrollment table, so unlink before deleting.
        s.getCourses().forEach(c -> c.getStudents().remove(s));
        students.delete(s);
    }

    Student get(Long id) {
        return students.findById(id).orElseThrow(() -> new NotFoundException("Student " + id + " not found"));
    }
}
