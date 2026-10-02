package com.college.aptitudesystem.config;

import com.college.aptitudesystem.entity.AssessmentDomain;
import com.college.aptitudesystem.repository.AssessmentDomainRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AssessmentDomainSeeder implements ApplicationRunner {

    private final AssessmentDomainRepository assessmentDomainRepository;

    public AssessmentDomainSeeder(AssessmentDomainRepository assessmentDomainRepository) {
        this.assessmentDomainRepository = assessmentDomainRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (assessmentDomainRepository.count() > 0) {
            return;
        }

        for (String domainName : AssessmentDomain.DEFAULT_DOMAIN_CHOICES) {
            if (!assessmentDomainRepository.existsByNameIgnoreCase(domainName)) {
                assessmentDomainRepository.save(new AssessmentDomain(domainName));
            }
        }
    }
}
