package com.college.aptitudesystem.controller;

import com.college.aptitudesystem.entity.AdminUser;
import com.college.aptitudesystem.entity.AssessmentDomain;
import com.college.aptitudesystem.entity.LiveTest;
import com.college.aptitudesystem.entity.Question;
import com.college.aptitudesystem.repository.AdminUserRepository;
import com.college.aptitudesystem.repository.AssessmentDomainRepository;
import com.college.aptitudesystem.repository.LiveTestRepository;
import com.college.aptitudesystem.repository.QuestionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class AdminController {

    private final QuestionRepository questionRepository;
    private final AdminUserRepository adminUserRepository;
    private final AssessmentDomainRepository assessmentDomainRepository;
    private final LiveTestRepository liveTestRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdminController(QuestionRepository questionRepository,
                          AdminUserRepository adminUserRepository,
                          AssessmentDomainRepository assessmentDomainRepository,
                          LiveTestRepository liveTestRepository) {
        this.questionRepository = questionRepository;
        this.adminUserRepository = adminUserRepository;
        this.assessmentDomainRepository = assessmentDomainRepository;
        this.liveTestRepository = liveTestRepository;
    }

    @GetMapping("/admin/login")
    public String adminLoginPage(HttpSession session) {
        if (Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"))) {
            return "redirect:/admin";
        }
        return "admin-login";
    }

    @PostMapping("/admin/login")
    public String loginAdmin(@RequestParam String username,
                            @RequestParam String password,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Username and password are required.");
            return "redirect:/admin/login";
        }

        Optional<AdminUser> adminUser = adminUserRepository.findByUsername(username.trim());
        if (adminUser.isPresent() && passwordEncoder.matches(password, adminUser.get().getPasswordHash())) {
            session.setAttribute("adminLoggedIn", true);
            session.setAttribute("adminUsername", adminUser.get().getUsername());
            return "redirect:/admin";
        }

        redirectAttributes.addFlashAttribute("error", "Invalid admin username or password.");
        return "redirect:/admin/login";
    }

    @GetMapping("/admin")
    public String adminPage(HttpSession session,
                            @RequestParam(required = false) String categoryFilter,
                            @RequestParam(required = false) String searchTerm,
                            Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"))) {
            return "redirect:/admin/login";
        }

        ensureDefaultDomains();

        String normalizedCategory = categoryFilter == null || categoryFilter.isBlank() || "All".equalsIgnoreCase(categoryFilter)
                ? "All" : categoryFilter.trim();
        String normalizedSearch = searchTerm == null ? "" : searchTerm.trim();

        List<String> categories = getConfiguredDomains();
        List<Question> allQuestions = questionRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        List<Question> filteredQuestions = allQuestions.stream()
                .filter(question -> "All".equals(normalizedCategory)
                        || question.getCategory().equalsIgnoreCase(normalizedCategory))
                .filter(question -> normalizedSearch.isEmpty()
                        || question.getQuestion().toLowerCase().contains(normalizedSearch.toLowerCase())
                        || question.getCategory().toLowerCase().contains(normalizedSearch.toLowerCase()))
                .toList();

        model.addAttribute("questions", filteredQuestions);
        model.addAttribute("adminUsername", session.getAttribute("adminUsername"));
        model.addAttribute("categories", categories);
        model.addAttribute("domains", assessmentDomainRepository.findAllByOrderByNameAsc());
        model.addAttribute("liveTests", liveTestRepository.findAllByOrderByStartTimeAsc());
        model.addAttribute("liveTestDomains", categories);
        model.addAttribute("selectedCategory", normalizedCategory);
        model.addAttribute("liveTestQuestions", questionRepository.findAll().stream()
                .filter(question -> question.getLiveTestId() != null)
                .sorted((a, b) -> a.getLiveTestId().compareTo(b.getLiveTestId()))
                .toList());
        model.addAttribute("searchTerm", normalizedSearch);

        var domainStats = new java.util.LinkedHashMap<String, Long>();
        for (String category : categories) {
            domainStats.put(category, questionRepository.countByCategoryIgnoreCase(category));
        }
        model.addAttribute("domainStats", domainStats);
        model.addAttribute("totalQuestions", allQuestions.size());
        model.addAttribute("activeDomains", (int) domainStats.values().stream().filter(count -> count > 0).count());
        model.addAttribute("latestQuestionId", allQuestions.isEmpty() ? 0 : allQuestions.get(allQuestions.size() - 1).getId());

        return "admin";
    }

    @GetMapping("/admin/logout")
    public String logoutAdmin(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }

    @PostMapping("/admin/questions/add")
    public String addQuestion(HttpSession session,
                              @RequestParam String question,
                              @RequestParam String optionA,
                              @RequestParam String optionB,
                              @RequestParam String optionC,
                              @RequestParam String optionD,
                              @RequestParam String correctAnswer,
                              @RequestParam String category,
                              RedirectAttributes redirectAttributes) {

        if (!Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"))) {
            return "redirect:/admin/login";
        }

        if (question == null || question.trim().isEmpty() || optionA == null || optionA.trim().isEmpty()
                || optionB == null || optionB.trim().isEmpty() || optionC == null || optionC.trim().isEmpty()
                || optionD == null || optionD.trim().isEmpty() || category == null || category.trim().isEmpty()
                || correctAnswer == null || correctAnswer.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "All fields are required.");
            return "redirect:/admin";
        }

        String normalizedCorrect = correctAnswer.trim().toUpperCase();
        if (!normalizedCorrect.matches("[A-D]")) {
            redirectAttributes.addFlashAttribute("error", "Correct answer must be A, B, C or D.");
            return "redirect:/admin";
        }

        String normalizedCategory = category.trim();
        if (!getConfiguredDomains().contains(normalizedCategory)) {
            redirectAttributes.addFlashAttribute("error", "Selected domain is not available.");
            return "redirect:/admin";
        }

        Question newQuestion = new Question();
        newQuestion.setQuestion(question.trim());
        newQuestion.setOptionA(optionA.trim());
        newQuestion.setOptionB(optionB.trim());
        newQuestion.setOptionC(optionC.trim());
        newQuestion.setOptionD(optionD.trim());
        newQuestion.setCorrectAnswer(normalizedCorrect);
        newQuestion.setCategory(normalizedCategory);

        questionRepository.save(newQuestion);
        redirectAttributes.addFlashAttribute("success", "Question added successfully.");
        return "redirect:/admin";
    }

    @PostMapping("/admin/questions/delete/{id}")
    public String deleteQuestion(HttpSession session,
                                @PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        if (!Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"))) {
            return "redirect:/admin/login";
        }

        if (questionRepository.existsById(id)) {
            questionRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Question deleted successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Question not found.");
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/domains/add")
    public String addDomain(HttpSession session,
                            @RequestParam String domainName,
                            RedirectAttributes redirectAttributes) {
        if (!Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"))) {
            return "redirect:/admin/login";
        }

        String normalizedDomain = domainName == null ? "" : domainName.trim();
        if (normalizedDomain.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Domain name is required.");
            return "redirect:/admin";
        }

        if (assessmentDomainRepository.existsByNameIgnoreCase(normalizedDomain)) {
            redirectAttributes.addFlashAttribute("error", "This domain already exists.");
            return "redirect:/admin";
        }

        assessmentDomainRepository.save(new AssessmentDomain(normalizedDomain));
        redirectAttributes.addFlashAttribute("success", "Domain added successfully.");
        return "redirect:/admin";
    }

    @PostMapping("/admin/domains/delete/{id}")
    public String deleteDomain(HttpSession session,
                              @PathVariable Long id,
                              RedirectAttributes redirectAttributes) {
        if (!Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"))) {
            return "redirect:/admin/login";
        }

        Optional<AssessmentDomain> domainToDelete = assessmentDomainRepository.findById(id);
        if (domainToDelete.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Domain not found.");
            return "redirect:/admin";
        }

        String domainName = domainToDelete.get().getName();
        questionRepository.deleteByCategoryIgnoreCase(domainName);
        assessmentDomainRepository.delete(domainToDelete.get());
        redirectAttributes.addFlashAttribute("success", "Domain deleted successfully.");
        return "redirect:/admin";
    }

    @PostMapping("/admin/domains/rename/{id}")
    public String renameDomain(HttpSession session,
                              @PathVariable Long id,
                              @RequestParam String newDomainName,
                              RedirectAttributes redirectAttributes) {
        if (!Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"))) {
            return "redirect:/admin/login";
        }

        Optional<AssessmentDomain> existingDomain = assessmentDomainRepository.findById(id);
        if (existingDomain.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Domain not found.");
            return "redirect:/admin";
        }

        String normalizedNewName = newDomainName == null ? "" : newDomainName.trim();
        if (normalizedNewName.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Domain name cannot be empty.");
            return "redirect:/admin";
        }

        String oldName = existingDomain.get().getName();
        if (!oldName.equalsIgnoreCase(normalizedNewName)
                && assessmentDomainRepository.existsByNameIgnoreCase(normalizedNewName)) {
            redirectAttributes.addFlashAttribute("error", "This domain already exists.");
            return "redirect:/admin";
        }

        existingDomain.get().setName(normalizedNewName);
        List<Question> questions = questionRepository.findByCategoryIgnoreCaseOrderByIdAsc(oldName);
        for (Question question : questions) {
            question.setCategory(normalizedNewName);
        }
        questionRepository.saveAll(questions);
        assessmentDomainRepository.save(existingDomain.get());

        redirectAttributes.addFlashAttribute("success", "Domain renamed successfully.");
        return "redirect:/admin";
    }

    @PostMapping("/admin/live-tests/questions/add")
    public String addLiveTestQuestion(HttpSession session,
                                     @RequestParam Long liveTestId,
                                     @RequestParam String liveTestQuestionDomain,
                                     @RequestParam String liveTestQuestion,
                                     @RequestParam String liveTestOptionA,
                                     @RequestParam String liveTestOptionB,
                                     @RequestParam String liveTestOptionC,
                                     @RequestParam String liveTestOptionD,
                                     @RequestParam String liveTestCorrectAnswer,
                                     RedirectAttributes redirectAttributes) {
        if (!Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"))) {
            return "redirect:/admin/login";
        }

        if (liveTestId == null || liveTestQuestionDomain == null || liveTestQuestion == null || liveTestQuestion.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a live test and fill in the question details.");
            return "redirect:/admin";
        }

        Optional<LiveTest> liveTest = liveTestRepository.findById(liveTestId);
        if (liveTest.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Live test not found.");
            return "redirect:/admin";
        }

        String normalizedDomain = liveTestQuestionDomain.trim();
        String normalizedAnswer = liveTestCorrectAnswer == null ? "" : liveTestCorrectAnswer.trim().toUpperCase();
        if (!normalizedAnswer.matches("[A-D]")) {
            redirectAttributes.addFlashAttribute("error", "Correct answer must be A, B, C, or D.");
            return "redirect:/admin";
        }

        Question question = new Question();
        question.setQuestion(liveTestQuestion.trim());
        question.setOptionA(liveTestOptionA == null ? "" : liveTestOptionA.trim());
        question.setOptionB(liveTestOptionB == null ? "" : liveTestOptionB.trim());
        question.setOptionC(liveTestOptionC == null ? "" : liveTestOptionC.trim());
        question.setOptionD(liveTestOptionD == null ? "" : liveTestOptionD.trim());
        question.setCorrectAnswer(normalizedAnswer);
        question.setCategory(normalizedDomain);
        question.setLiveTestId(liveTestId);
        questionRepository.save(question);

        redirectAttributes.addFlashAttribute("success", "Live test question added successfully.");
        return "redirect:/admin";
    }

    @PostMapping("/admin/live-tests/questions/bulk-add")
    public String addBulkLiveTestQuestions(HttpSession session,
                                          @RequestParam Long liveTestId,
                                          HttpServletRequest request,
                                          RedirectAttributes redirectAttributes) {
        if (!Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"))) {
            return "redirect:/admin/login";
        }

        if (liveTestId == null) {
            redirectAttributes.addFlashAttribute("error", "Please select a live test before adding questions.");
            return "redirect:/admin";
        }

        Optional<LiveTest> liveTest = liveTestRepository.findById(liveTestId);
        if (liveTest.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Live test not found.");
            return "redirect:/admin";
        }

        List<Question> questionsToSave = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            String questionText = request.getParameter("question_" + i);
            if (questionText == null || questionText.trim().isEmpty()) {
                continue;
            }

            String answerValue = request.getParameter("correctAnswer_" + i);
            if (answerValue == null || !answerValue.trim().matches("[A-D]")) {
                redirectAttributes.addFlashAttribute("error", "Question " + i + " has an invalid answer. Use A, B, C, or D.");
                return "redirect:/admin";
            }

            String optionA = request.getParameter("optionA_" + i);
            String optionB = request.getParameter("optionB_" + i);
            String optionC = request.getParameter("optionC_" + i);
            String optionD = request.getParameter("optionD_" + i);
            if (optionA == null || optionB == null || optionC == null || optionD == null ||
                    optionA.trim().isEmpty() || optionB.trim().isEmpty() || optionC.trim().isEmpty() || optionD.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Question " + i + " must include all 4 options.");
                return "redirect:/admin";
            }

            Question question = new Question();
            question.setQuestion(questionText.trim());
            question.setOptionA(optionA.trim());
            question.setOptionB(optionB.trim());
            question.setOptionC(optionC.trim());
            question.setOptionD(optionD.trim());
            question.setCorrectAnswer(answerValue.trim().toUpperCase());
            question.setCategory(liveTest.get().getDomain());
            question.setLiveTestId(liveTestId);
            questionsToSave.add(question);
        }

        if (questionsToSave.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please add at least one valid question for the live test.");
            return "redirect:/admin";
        }

        if (questionsToSave.size() > 10) {
            redirectAttributes.addFlashAttribute("error", "A live test can have a maximum of 10 questions.");
            return "redirect:/admin";
        }

        questionRepository.saveAll(questionsToSave);
        redirectAttributes.addFlashAttribute("success", "Live test questions saved successfully.");
        return "redirect:/admin";
    }

    @PostMapping("/admin/live-tests/add")
    public String addLiveTest(HttpSession session,
                             @RequestParam String liveTestName,
                             @RequestParam String liveTestDomain,
                             @RequestParam String startTime,
                             @RequestParam String endTime,
                             RedirectAttributes redirectAttributes) {
        if (!Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"))) {
            return "redirect:/admin/login";
        }

        String normalizedName = liveTestName == null ? "" : liveTestName.trim();
        String normalizedDomain = liveTestDomain == null ? "" : liveTestDomain.trim();
        if (normalizedName.isEmpty() || normalizedDomain.isEmpty() || startTime == null || endTime == null) {
            redirectAttributes.addFlashAttribute("error", "Live test name, domain, start time and end time are required.");
            return "redirect:/admin";
        }

        if (!getConfiguredDomains().contains(normalizedDomain)) {
            redirectAttributes.addFlashAttribute("error", "Selected domain is not available.");
            return "redirect:/admin";
        }

        LocalDateTime start = LocalDateTime.parse(startTime.replace("T", "T"));
        LocalDateTime end = LocalDateTime.parse(endTime.replace("T", "T"));

        if (!end.isAfter(start)) {
            redirectAttributes.addFlashAttribute("error", "End time must be after the start time.");
            return "redirect:/admin";
        }

        liveTestRepository.save(new LiveTest(normalizedName, normalizedDomain, start, end));
        redirectAttributes.addFlashAttribute("success", "Live test scheduled successfully.");
        return "redirect:/admin";
    }

    @PostMapping("/admin/live-tests/delete/{id}")
    public String deleteLiveTest(HttpSession session,
                                @PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        if (!Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"))) {
            return "redirect:/admin/login";
        }

        if (liveTestRepository.existsById(id)) {
            liveTestRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Live test removed successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Live test not found.");
        }
        return "redirect:/admin";
    }

    private List<String> getConfiguredDomains() {
        ensureDefaultDomains();
        List<AssessmentDomain> persistedDomains = assessmentDomainRepository.findAllByOrderByNameAsc();
        if (persistedDomains == null || persistedDomains.isEmpty()) {
            return new ArrayList<>(AssessmentDomain.DEFAULT_DOMAIN_CHOICES);
        }

        List<String> configuredDomains = new ArrayList<>();
        for (AssessmentDomain domain : persistedDomains) {
            if (domain != null && domain.getName() != null && !domain.getName().isBlank()) {
                configuredDomains.add(domain.getName());
            }
        }
        return configuredDomains;
    }

    private void ensureDefaultDomains() {
        List<AssessmentDomain> persistedDomains = assessmentDomainRepository.findAllByOrderByNameAsc();
        if (persistedDomains != null && !persistedDomains.isEmpty()) {
            return;
        }

        for (String domainName : AssessmentDomain.DEFAULT_DOMAIN_CHOICES) {
            if (!assessmentDomainRepository.existsByNameIgnoreCase(domainName)) {
                assessmentDomainRepository.save(new AssessmentDomain(domainName));
            }
        }
    }

}
