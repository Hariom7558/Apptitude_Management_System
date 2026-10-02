package com.college.aptitudesystem.controller;

import com.college.aptitudesystem.entity.AssessmentDomain;
import com.college.aptitudesystem.entity.LiveTest;
import com.college.aptitudesystem.entity.Question;
import com.college.aptitudesystem.entity.TestResult;
import com.college.aptitudesystem.repository.AssessmentDomainRepository;
import com.college.aptitudesystem.repository.LiveTestRepository;
import com.college.aptitudesystem.repository.QuestionRepository;
import com.college.aptitudesystem.repository.TestResultRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class StudentController {

    private final QuestionRepository questionRepository;
    private final TestResultRepository testResultRepository;
    private final AssessmentDomainRepository assessmentDomainRepository;
    private final LiveTestRepository liveTestRepository;

    public StudentController(QuestionRepository questionRepository,
                            TestResultRepository testResultRepository,
                            AssessmentDomainRepository assessmentDomainRepository,
                            LiveTestRepository liveTestRepository) {
        this.questionRepository = questionRepository;
        this.testResultRepository = testResultRepository;
        this.assessmentDomainRepository = assessmentDomainRepository;
        this.liveTestRepository = liveTestRepository;
    }

    @GetMapping({"/", "/login"})
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String loginStudent(@RequestParam String studentName,
                              @RequestParam String studentEmail,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (studentName == null || studentName.trim().isEmpty() || studentEmail == null || studentEmail.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please enter your name and email.");
            return "redirect:/login";
        }

        session.setAttribute("studentName", studentName.trim());
        session.setAttribute("studentEmail", studentEmail.trim());
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (session.getAttribute("studentName") == null) {
            return "redirect:/login";
        }

        model.addAttribute("studentName", session.getAttribute("studentName"));
        model.addAttribute("questionCount", questionRepository.count());
        model.addAttribute("domainCards", buildDomainCards());
        model.addAttribute("liveTests", buildLiveTestSummaries());
        return "dashboard";
    }

    @GetMapping("/test")
    public String showTest(HttpSession session,
                          @RequestParam(required = false) String category,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (session.getAttribute("studentName") == null) {
            return "redirect:/login";
        }

        String selectedCategory = normalizeCategory(category);
        List<Question> questions = findQuestionsForCategory(selectedCategory);
        if (questions.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No questions available for this domain yet. Please contact the admin.");
            return "redirect:/dashboard";
        }

        model.addAttribute("questions", questions);
        model.addAttribute("selectedCategory", selectedCategory == null ? "All Domains" : selectedCategory);
        return "test";
    }

    @GetMapping("/live-test/{id}")
    public String showLiveTest(@PathVariable Long id,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (session.getAttribute("studentName") == null) {
            return "redirect:/login";
        }

        LiveTest liveTest = liveTestRepository.findById(id).orElse(null);
        if (liveTest == null) {
            redirectAttributes.addFlashAttribute("error", "Live test not found.");
            return "redirect:/dashboard";
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(liveTest.getStartTime())) {
            redirectAttributes.addFlashAttribute("error", "This live test has not started yet.");
            return "redirect:/dashboard";
        }
        if (now.isAfter(liveTest.getEndTime())) {
            return "redirect:/live-test/" + id + "/leaderboard";
        }

        List<Question> questions = questionRepository.findByLiveTestIdOrderByIdAsc(liveTest.getId());
        if (questions.isEmpty()) {
            questions = questionRepository.findByCategoryIgnoreCaseOrderByIdAsc(liveTest.getDomain());
        }
        if (questions.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No questions are available for this live test yet.");
            return "redirect:/dashboard";
        }

        model.addAttribute("liveTest", liveTest);
        model.addAttribute("questions", questions);
        model.addAttribute("studentName", session.getAttribute("studentName"));
        model.addAttribute("remainingSeconds", Duration.between(now, liveTest.getEndTime()).getSeconds());
        return "live-test";
    }

    @GetMapping("/live-test/{id}/leaderboard")
    public String showLiveTestLeaderboard(@PathVariable Long id,
                                         HttpSession session,
                                         Model model,
                                         RedirectAttributes redirectAttributes) {
        if (session.getAttribute("studentName") == null) {
            return "redirect:/login";
        }

        LiveTest liveTest = liveTestRepository.findById(id).orElse(null);
        if (liveTest == null) {
            redirectAttributes.addFlashAttribute("error", "Live test not found.");
            return "redirect:/dashboard";
        }

        List<TestResult> leaderboard = testResultRepository.findByLiveTestIdOrderByPercentageDescSubmittedAtAsc(id);
        int rank = 0;
        for (TestResult result : leaderboard) {
            rank += 1;
            result.setRankPosition(rank);
        }

        model.addAttribute("liveTest", liveTest);
        model.addAttribute("leaderboard", leaderboard);
        model.addAttribute("studentEmail", session.getAttribute("studentEmail"));
        model.addAttribute("studentResult", testResultRepository.findFirstByStudentEmailAndLiveTestIdOrderBySubmittedAtDesc((String) session.getAttribute("studentEmail"), id).orElse(null));
        return "live-test-results";
    }

    @PostMapping("/live-test/{id}/submit")
    public String submitLiveTest(@PathVariable Long id,
                                HttpServletRequest request,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        String studentName = (String) session.getAttribute("studentName");
        String studentEmail = (String) session.getAttribute("studentEmail");

        if (studentName == null || studentEmail == null) {
            return "redirect:/login";
        }

        LiveTest liveTest = liveTestRepository.findById(id).orElse(null);
        if (liveTest == null) {
            redirectAttributes.addFlashAttribute("error", "Live test not found.");
            return "redirect:/dashboard";
        }

        Optional<TestResult> existingSubmission = testResultRepository.findFirstByStudentEmailAndLiveTestIdOrderBySubmittedAtDesc(studentEmail, id);
        if (existingSubmission.isPresent()) {
            return "redirect:/live-test/" + id + "/leaderboard";
        }

        List<Question> questions = questionRepository.findByLiveTestIdOrderByIdAsc(liveTest.getId());
        if (questions.isEmpty()) {
            questions = questionRepository.findByCategoryIgnoreCaseOrderByIdAsc(liveTest.getDomain());
        }
        int totalQuestions = questions.size();
        int correctAnswers = 0;

        for (Question question : questions) {
            String submitted = request.getParameter("answer_" + question.getId());
            if (submitted != null && submitted.equalsIgnoreCase(question.getCorrectAnswer())) {
                correctAnswers++;
            }
        }

        int score = correctAnswers;
        double percentage = totalQuestions == 0 ? 0.0 : ((double) correctAnswers / totalQuestions) * 100.0;
        percentage = Math.round(percentage * 100.0) / 100.0;

        TestResult result = new TestResult();
        result.setStudentName(studentName);
        result.setStudentEmail(studentEmail);
        result.setTotalQuestions(totalQuestions);
        result.setCorrectAnswers(correctAnswers);
        result.setScore(score);
        result.setPercentage(percentage);
        result.setLiveTestId(id);
        result.setLiveTestName(liveTest.getName());
        result.setLiveTestDomain(liveTest.getDomain());
        result.setAssessmentName(liveTest.getName());
        result.setAssessmentDomain(liveTest.getDomain());
        testResultRepository.save(result);

        return "redirect:/live-test/" + id + "/leaderboard";
    }

    @PostMapping("/test/submit")
    public String submitTest(HttpServletRequest request, HttpSession session, Model model) {
        String studentName = (String) session.getAttribute("studentName");
        String studentEmail = (String) session.getAttribute("studentEmail");

        if (studentName == null || studentEmail == null) {
            return "redirect:/login";
        }

        String selectedCategory = normalizeCategory(request.getParameter("selectedCategory"));
        List<Question> questions = findQuestionsForCategory(selectedCategory);
        int totalQuestions = questions.size();
        int correctAnswers = 0;

        for (Question question : questions) {
            String submitted = request.getParameter("answer_" + question.getId());
            if (submitted != null && submitted.equalsIgnoreCase(question.getCorrectAnswer())) {
                correctAnswers++;
            }
        }

        int score = correctAnswers;
        double percentage = totalQuestions == 0 ? 0.0 : ((double) correctAnswers / totalQuestions) * 100.0;
        percentage = Math.round(percentage * 100.0) / 100.0;

        TestResult result = new TestResult();
        result.setStudentName(studentName);
        result.setStudentEmail(studentEmail);
        result.setTotalQuestions(totalQuestions);
        result.setCorrectAnswers(correctAnswers);
        result.setScore(score);
        result.setPercentage(percentage);
        result.setAssessmentName(selectedCategory == null || selectedCategory.isBlank() ? "Practice Test" : selectedCategory + " Practice");
        result.setAssessmentDomain(selectedCategory == null || selectedCategory.isBlank() ? "General" : selectedCategory);
        testResultRepository.save(result);

        model.addAttribute("studentName", studentName);
        model.addAttribute("selectedCategory", selectedCategory == null ? "All Domains" : selectedCategory);
        model.addAttribute("totalQuestions", totalQuestions);
        model.addAttribute("correctAnswers", correctAnswers);
        model.addAttribute("score", score);
        model.addAttribute("percentage", percentage);
        return "result";
    }

    @GetMapping("/results")
    public String showResults(HttpSession session, Model model) {
        String studentEmail = (String) session.getAttribute("studentEmail");
        if (studentEmail == null) {
            return "redirect:/login";
        }

        List<TestResult> results = testResultRepository.findByStudentEmailOrderBySubmittedAtDesc(studentEmail);
        model.addAttribute("results", results);
        return "results";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    private List<LiveTestSummary> buildLiveTestSummaries() {
        LocalDateTime now = LocalDateTime.now();
        List<LiveTestSummary> summaries = new ArrayList<>();
        for (LiveTest liveTest : liveTestRepository.findAllByOrderByStartTimeAsc()) {
            summaries.add(new LiveTestSummary(liveTest, now));
        }
        return summaries;
    }

    private List<DomainSummary> buildDomainCards() {
        List<DomainSummary> domainCards = new ArrayList<>();
        for (String domain : getConfiguredDomains()) {
            long count = questionRepository.countByCategoryIgnoreCase(domain);
            domainCards.add(new DomainSummary(domain, count));
        }
        return domainCards;
    }

    private List<String> getConfiguredDomains() {
        List<String> domainNames = assessmentDomainRepository.findAllByOrderByNameAsc()
                .stream()
                .map(AssessmentDomain::getName)
                .filter(name -> name != null && !name.isBlank())
                .toList();

        if (!domainNames.isEmpty()) {
            return domainNames;
        }
        return new ArrayList<>(AssessmentDomain.DEFAULT_DOMAIN_CHOICES);
    }

    private List<Question> findQuestionsForCategory(String category) {
        if (category == null || category.isBlank() || "all".equalsIgnoreCase(category) || "all domains".equalsIgnoreCase(category)) {
            return questionRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        }
        return questionRepository.findByCategoryIgnoreCaseOrderByIdAsc(category);
    }

    private String normalizeCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return null;
        }
        return category.trim();
    }

    public static class DomainSummary {
        private final String name;
        private final long count;

        public DomainSummary(String name, long count) {
            this.name = name;
            this.count = count;
        }

        public String getName() {
            return name;
        }

        public long getCount() {
            return count;
        }
    }

    public static class LiveTestSummary {
        private final LiveTest liveTest;
        private final String status;
        private final String dateLabel;
        private final String timeLabel;

        public LiveTestSummary(LiveTest liveTest, LocalDateTime now) {
            this.liveTest = liveTest;
            if (now.isBefore(liveTest.getStartTime())) {
                this.status = "Upcoming";
            } else if (now.isAfter(liveTest.getEndTime())) {
                this.status = "Completed";
            } else {
                this.status = "Live Now";
            }
            this.dateLabel = liveTest.getStartTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
            this.timeLabel = liveTest.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                    + " - " + liveTest.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        }

        public Long getId() { return liveTest.getId(); }
        public String getName() { return liveTest.getName(); }
        public String getDomain() { return liveTest.getDomain(); }
        public LocalDateTime getStartTime() { return liveTest.getStartTime(); }
        public LocalDateTime getEndTime() { return liveTest.getEndTime(); }
        public String getStatus() { return status; }
        public String getDateLabel() { return dateLabel; }
        public String getTimeLabel() { return timeLabel; }
        public boolean isUpcoming() { return "Upcoming".equals(status); }
        public boolean isLiveNow() { return "Live Now".equals(status); }
        public boolean isCompleted() { return "Completed".equals(status); }
    }
}
