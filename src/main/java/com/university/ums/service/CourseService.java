package com.university.ums.service;

import com.university.ums.dto.Dtos.CourseRequest;
import com.university.ums.dto.Dtos.CourseResponse;
import com.university.ums.exception.BadRequestException;
import com.university.ums.exception.NotFoundException;
import com.university.ums.model.Course;
import com.university.ums.model.Student;
import com.university.ums.repository.CourseRepository;
import com.university.ums.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CourseService {

    private final CourseRepository courses;
    private final StudentRepository studentRepo;
    private final StudentService students;
    private final TeacherService teachers;

    public CourseService(CourseRepository courses, StudentRepository studentRepo,
                         StudentService students, TeacherService teachers) {
        this.courses = courses;
        this.studentRepo = studentRepo;
        this.students = students;
        this.teachers = teachers;
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> findAll() {
        return courses.findAll().stream().map(CourseResponse::from).toList();
    }

    public boolean existsByCode(String code) {
        return courses.existsByCode(code);
    }

    @Transactional(readOnly = true)
    public CourseResponse findById(Long id) {
        return CourseResponse.from(get(id));
    }

    public CourseResponse create(CourseRequest req) {
        if (courses.existsByCode(req.code())) {
            throw new BadRequestException("course code '" + req.code() + "' already exists");
        }
        Course c = new Course(req.code(), req.title(), req.credits());
        c.setTeacher(req.teacherId() == null ? null : teachers.get(req.teacherId()));
        return CourseResponse.from(courses.save(c));
    }

    public CourseResponse update(Long id, CourseRequest req) {
        Course c = get(id);
        if (!c.getCode().equals(req.code()) && courses.existsByCode(req.code())) {
            throw new BadRequestException("course code '" + req.code() + "' already exists");
        }
        c.setCode(req.code());
        c.setTitle(req.title());
        c.setCredits(req.credits());
        c.setTeacher(req.teacherId() == null ? null : teachers.get(req.teacherId()));
        return CourseResponse.from(c);
    }

    public void delete(Long id) {
        courses.delete(get(id));
    }

    public CourseResponse enroll(Long courseId, Long studentId) {
        Course c = get(courseId);
        c.getStudents().add(students.get(studentId));
        return CourseResponse.from(c);
    }

    public CourseResponse unenroll(Long courseId, Long studentId) {
        Course c = get(courseId);
        c.getStudents().remove(students.get(studentId));
        return CourseResponse.from(c);
    }

    /** Lets a logged-in student enroll / drop themselves. */
    public CourseResponse enrollSelf(Long courseId, String username, boolean enroll) {
        Student s = studentRepo.findByUserUsername(username)
                .orElseThrow(() -> new NotFoundException("No student profile for " + username));
        return enroll ? enroll(courseId, s.getId()) : unenroll(courseId, s.getId());
    }

    private Course get(Long id) {
        return courses.findById(id).orElseThrow(() -> new NotFoundException("Course " + id + " not found"));
    }
}
