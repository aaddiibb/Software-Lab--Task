package com.university.ums.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "teachers")
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String department;

    private String designation;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    @JoinColumn(name = "user_id", unique = true)
    private AppUser user;

    @OneToMany(mappedBy = "teacher")
    private Set<Course> courses = new HashSet<>();

    protected Teacher() {
    }

    public Teacher(String name, String email, String department, String designation, AppUser user) {
        this.name = name;
        this.email = email;
        this.department = department;
        this.designation = designation;
        this.user = user;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public AppUser getUser() { return user; }
    public Set<Course> getCourses() { return courses; }
}
