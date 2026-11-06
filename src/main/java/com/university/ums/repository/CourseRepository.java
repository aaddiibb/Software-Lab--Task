package com.university.ums.repository;

import com.university.ums.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByCode(String code);

    boolean existsByIdAndTeacherUserUsername(Long id, String username);
}
