package com.university.ums.repository;

import com.university.ums.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByUserUsername(String username);

    boolean existsByEmail(String email);
}
