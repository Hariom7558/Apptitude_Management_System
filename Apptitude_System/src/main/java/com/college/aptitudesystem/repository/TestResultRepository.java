package com.college.aptitudesystem.repository;

import com.college.aptitudesystem.entity.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    List<TestResult> findByStudentEmailOrderBySubmittedAtDesc(String studentEmail);
    List<TestResult> findByLiveTestIdOrderByPercentageDescSubmittedAtAsc(Long liveTestId);
    Optional<TestResult> findFirstByStudentEmailAndLiveTestIdOrderBySubmittedAtDesc(String studentEmail, Long liveTestId);
}
