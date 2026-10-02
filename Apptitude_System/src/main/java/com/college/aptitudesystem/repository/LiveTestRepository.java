package com.college.aptitudesystem.repository;

import com.college.aptitudesystem.entity.LiveTest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LiveTestRepository extends JpaRepository<LiveTest, Long> {
    List<LiveTest> findAllByOrderByStartTimeAsc();
}
