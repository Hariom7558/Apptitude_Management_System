package com.college.aptitudesystem.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "optionA", nullable = false)
    private String optionA;

    @Column(name = "optionB", nullable = false)
    private String optionB;

    @Column(name = "optionC", nullable = false)
    private String optionC;

    @Column(name = "optionD", nullable = false)
    private String optionD;

    @Column(name = "correctAnswer", nullable = false)
    private String correctAnswer;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "liveTestId")
    private Long liveTestId;

    public Question() {
    }

    public Question(String question, String optionA, String optionB, String optionC, String optionD,
                    String correctAnswer, String category) {
        this.question = question;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
        this.category = category;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }
    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }
    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }
    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Long getLiveTestId() { return liveTestId; }
    public void setLiveTestId(Long liveTestId) { this.liveTestId = liveTestId; }
}
