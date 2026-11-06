package com.university.ums.repository;

import com.university.ums.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByUserUsername(String username);

    boolean existsByEmail(String email);
}
