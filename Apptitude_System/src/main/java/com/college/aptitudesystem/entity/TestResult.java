package com.college.aptitudesystem.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_results")
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "studentName", nullable = false)
    private String studentName;

    @Column(name = "studentEmail", nullable = false)
    private String studentEmail;

    @Column(name = "totalQuestions", nullable = false)
    private Integer totalQuestions;

    @Column(name = "correctAnswers", nullable = false)
    private Integer correctAnswers;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "percentage", nullable = false)
    private Double percentage;

    @Column(name = "submittedAt", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "liveTestId")
    private Long liveTestId;

    @Column(name = "liveTestName")
    private String liveTestName;

    @Column(name = "liveTestDomain")
    private String liveTestDomain;

    @Column(name = "assessmentName")
    private String assessmentName;

    @Column(name = "assessmentDomain")
    private String assessmentDomain;

    @Column(name = "rankPosition")
    private Integer rankPosition;

    @PrePersist
    public void prePersist() {
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }
    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }
    public Integer getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(Integer correctAnswers) { this.correctAnswers = correctAnswers; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Double getPercentage() { return percentage; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public Long getLiveTestId() { return liveTestId; }
    public void setLiveTestId(Long liveTestId) { this.liveTestId = liveTestId; }
    public String getLiveTestName() { return liveTestName; }
    public void setLiveTestName(String liveTestName) { this.liveTestName = liveTestName; }
    public String getLiveTestDomain() { return liveTestDomain; }
    public void setLiveTestDomain(String liveTestDomain) { this.liveTestDomain = liveTestDomain; }
    public String getAssessmentName() { return assessmentName; }
    public void setAssessmentName(String assessmentName) { this.assessmentName = assessmentName; }
    public String getAssessmentDomain() { return assessmentDomain; }
    public void setAssessmentDomain(String assessmentDomain) { this.assessmentDomain = assessmentDomain; }
    public Integer getRankPosition() { return rankPosition; }
    public void setRankPosition(Integer rankPosition) { this.rankPosition = rankPosition; }
}
