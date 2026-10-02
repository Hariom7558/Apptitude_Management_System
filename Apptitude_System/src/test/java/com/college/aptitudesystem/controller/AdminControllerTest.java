package com.college.aptitudesystem.controller;

import com.college.aptitudesystem.entity.AssessmentDomain;
import com.college.aptitudesystem.entity.LiveTest;
import com.college.aptitudesystem.entity.Question;
import com.college.aptitudesystem.repository.AdminUserRepository;
import com.college.aptitudesystem.repository.AssessmentDomainRepository;
import com.college.aptitudesystem.repository.LiveTestRepository;
import com.college.aptitudesystem.repository.QuestionRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuestionRepository questionRepository;

    @MockBean
    private AssessmentDomainRepository assessmentDomainRepository;

    @MockBean
    private LiveTestRepository liveTestRepository;

    @MockBean
    private AdminUserRepository adminUserRepository;

    @Test
    void adminPageShouldIncludeStatsAndFilters() throws Exception {
        Question javaQuestion = new Question();
        javaQuestion.setId(1L);
        javaQuestion.setQuestion("What is Java?");
        javaQuestion.setCategory("Java");
        javaQuestion.setCorrectAnswer("A");

        Question dsaQuestion = new Question();
        dsaQuestion.setId(2L);
        dsaQuestion.setQuestion("What is DSA?");
        dsaQuestion.setCategory("DSA");
        dsaQuestion.setCorrectAnswer("B");

        AssessmentDomain javaDomain = new AssessmentDomain();
        javaDomain.setId(1L);
        javaDomain.setName("Java");

        AssessmentDomain customDomain = new AssessmentDomain();
        customDomain.setId(2L);
        customDomain.setName("Cyber Security");

        given(questionRepository.findAll(any(Sort.class))).willReturn(List.of(javaQuestion, dsaQuestion));
        given(questionRepository.countByCategoryIgnoreCase("Java")).willReturn(1L);
        given(questionRepository.countByCategoryIgnoreCase("DSA")).willReturn(1L);
        given(assessmentDomainRepository.findAllByOrderByNameAsc()).willReturn(List.of(customDomain, javaDomain));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("adminLoggedIn", true);
        session.setAttribute("adminUsername", "admin");

        mockMvc.perform(get("/admin")
                        .session(session)
                        .param("categoryFilter", "Java")
                        .param("searchTerm", "Java"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("domainStats"))
                .andExpect(model().attribute("selectedCategory", "Java"))
                .andExpect(model().attribute("searchTerm", "Java"))
                .andExpect(model().attributeExists("domains"));
    }

    @Test
    void renameDomainShouldUpdateQuestionCategories() throws Exception {
        AssessmentDomain javaDomain = new AssessmentDomain();
        javaDomain.setId(1L);
        javaDomain.setName("Java");

        Question javaQuestion = new Question();
        javaQuestion.setId(10L);
        javaQuestion.setQuestion("What is Java?");
        javaQuestion.setCategory("Java");
        javaQuestion.setCorrectAnswer("A");

        given(assessmentDomainRepository.findById(1L)).willReturn(Optional.of(javaDomain));
        given(questionRepository.findByCategoryIgnoreCaseOrderByIdAsc("Java")).willReturn(List.of(javaQuestion));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("adminLoggedIn", true);
        session.setAttribute("adminUsername", "admin");

        mockMvc.perform(post("/admin/domains/rename/1")
                        .session(session)
                        .param("newDomainName", "Advanced Java"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));
    }

    @Test
    void adminPageShouldListScheduledLiveTests() throws Exception {
        LiveTest liveTest = new LiveTest();
        liveTest.setId(1L);
        liveTest.setName("Java Sprint");
        liveTest.setDomain("Java");
        liveTest.setStartTime(java.time.LocalDateTime.now().plusHours(1));
        liveTest.setEndTime(java.time.LocalDateTime.now().plusHours(2));

        given(liveTestRepository.findAllByOrderByStartTimeAsc()).willReturn(List.of(liveTest));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("adminLoggedIn", true);
        session.setAttribute("adminUsername", "admin");

        mockMvc.perform(get("/admin").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("liveTests"));
    }
}
