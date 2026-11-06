package com.university.ums.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String department;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    @JoinColumn(name = "user_id", unique = true)
    private AppUser user;

    @ManyToMany(mappedBy = "students")
    private Set<Course> courses = new HashSet<>();

    protected Student() {
    }

    public Student(String name, String email, String department, AppUser user) {
        this.name = name;
        this.email = email;
        this.department = department;
        this.user = user;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public AppUser getUser() { return user; }
    public Set<Course> getCourses() { return courses; }
}
