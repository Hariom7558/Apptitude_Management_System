package com.college.aptitudesystem.repository;

import com.college.aptitudesystem.entity.AssessmentDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssessmentDomainRepository extends JpaRepository<AssessmentDomain, Long> {
    List<AssessmentDomain> findAllByOrderByNameAsc();
    Optional<AssessmentDomain> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    void deleteByNameIgnoreCase(String name);
}
