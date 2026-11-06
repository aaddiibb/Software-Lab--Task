package com.university.ums.service;

import com.university.ums.dto.Dtos.TeacherRequest;
import com.university.ums.dto.Dtos.TeacherResponse;
import com.university.ums.exception.BadRequestException;
import com.university.ums.exception.NotFoundException;
import com.university.ums.model.Role;
import com.university.ums.model.Teacher;
import com.university.ums.repository.TeacherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TeacherService {

    private final TeacherRepository teachers;
    private final AccountService accounts;

    public TeacherService(TeacherRepository teachers, AccountService accounts) {
        this.teachers = teachers;
        this.accounts = accounts;
    }

    @Transactional(readOnly = true)
    public List<TeacherResponse> findAll() {
        return teachers.findAll().stream().map(TeacherResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TeacherResponse findById(Long id) {
        return TeacherResponse.from(get(id));
    }

    @Transactional(readOnly = true)
    public TeacherResponse findByUsername(String username) {
        return teachers.findByUserUsername(username)
                .map(TeacherResponse::from)
                .orElseThrow(() -> new NotFoundException("No teacher profile for " + username));
    }

    public TeacherResponse create(TeacherRequest req) {
        if (teachers.existsByEmail(req.email())) {
            throw new BadRequestException("email '" + req.email() + "' is already used");
        }
        Teacher t = new Teacher(req.name(), req.email(), req.department(), req.designation(),
                accounts.newAccount(req.username(), req.password(), Role.TEACHER));
        return TeacherResponse.from(teachers.save(t));
    }

    public TeacherResponse update(Long id, TeacherRequest req) {
        Teacher t = get(id);
        return update(t, req);
    }

    /** Updates only the teacher profile belonging to the authenticated username. */
    public TeacherResponse updateByUsername(String username, TeacherRequest req) {
        Teacher t = teachers.findByUserUsername(username)
                .orElseThrow(() -> new NotFoundException("No teacher profile for " + username));
        return update(t, req);
    }

    private TeacherResponse update(Teacher t, TeacherRequest req) {
        if (!t.getEmail().equals(req.email()) && teachers.existsByEmail(req.email())) {
            throw new BadRequestException("email '" + req.email() + "' is already used");
        }
        t.setName(req.name());
        t.setEmail(req.email());
        t.setDepartment(req.department());
        t.setDesignation(req.designation());
        accounts.updateAccount(t.getUser(), req.username(), req.password());
        return TeacherResponse.from(t);
    }

    public void delete(Long id) {
        Teacher t = get(id);
        // Keep the courses, just leave them without a teacher.
        t.getCourses().forEach(c -> c.setTeacher(null));
        teachers.delete(t);
    }

    Teacher get(Long id) {
        return teachers.findById(id).orElseThrow(() -> new NotFoundException("Teacher " + id + " not found"));
    }
}
