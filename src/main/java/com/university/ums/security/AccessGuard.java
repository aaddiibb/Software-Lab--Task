package com.university.ums.security;

import com.university.ums.repository.CourseRepository;
import com.university.ums.repository.StudentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Ownership checks used from @PreAuthorize expressions, e.g.
 * {@code @PreAuthorize("hasRole('ADMIN') or @guard.isCourseTeacher(#id, authentication)")}.
 */
@Component("guard")
public class AccessGuard {

    private final StudentRepository students;
    private final CourseRepository courses;

    public AccessGuard(StudentRepository students, CourseRepository courses) {
        this.students = students;
        this.courses = courses;
    }

    /** True if the logged-in user is the student with this id. */
    public boolean isSelfStudent(Long studentId, Authentication auth) {
        return students.findByUserUsername(auth.getName())
                .map(s -> s.getId().equals(studentId))
                .orElse(false);
    }

    /** True if the logged-in user is the teacher assigned to this course. */
    public boolean isCourseTeacher(Long courseId, Authentication auth) {
        return courses.existsByIdAndTeacherUserUsername(courseId, auth.getName());
    }
}
