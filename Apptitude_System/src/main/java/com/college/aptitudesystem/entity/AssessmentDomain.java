package com.college.aptitudesystem.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "assessment_domains")
public class AssessmentDomain {

    public static final List<String> DEFAULT_DOMAIN_CHOICES = List.of(
            "DSA",
            "Java",
            "C",
            "C++",
            "MySQL",
            "Python",
            "HTML/CSS",
            "JavaScript",
            "Data Science",
            "Computer Applications"
    );

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    public AssessmentDomain() {
    }

    public AssessmentDomain(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
