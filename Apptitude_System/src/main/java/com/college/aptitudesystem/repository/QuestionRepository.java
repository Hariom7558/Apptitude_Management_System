package com.college.aptitudesystem.repository;

import com.college.aptitudesystem.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByCategoryIgnoreCaseOrderByIdAsc(String category);
    List<Question> findByLiveTestIdOrderByIdAsc(Long liveTestId);
    long countByCategoryIgnoreCase(String category);
    void deleteByCategoryIgnoreCase(String category);
    void deleteByLiveTestId(Long liveTestId);
}
