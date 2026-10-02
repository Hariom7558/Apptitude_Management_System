package com.college.aptitudesystem.config;

import com.college.aptitudesystem.entity.Question;
import com.college.aptitudesystem.repository.QuestionRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AssessmentQuestionSeeder implements ApplicationRunner {

    private final QuestionRepository questionRepository;

    public AssessmentQuestionSeeder(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> validDomains = List.of(
                "DSA",
                "Java",
                "C",
                "C++",
                "MySQL",
                "Python",
                "HTML/CSS",
                "JavaScript",
                "Data Science",
                "Computer Applications"
        );

        boolean hasSeededDomains = validDomains.stream().anyMatch(domain -> questionRepository.countByCategoryIgnoreCase(domain) > 0);
        if (hasSeededDomains) {
            return;
        }

        questionRepository.deleteAll();

        List<Question> questions = List.of(
                buildQuestion("What is the time complexity of binary search on a sorted array?", "O(log n)", "O(n)", "O(n log n)", "O(1)", "A", "DSA"),
                buildQuestion("Which data structure follows FIFO order?", "Stack", "Queue", "Tree", "Graph", "B", "DSA"),

                buildQuestion("Which keyword is used to create a class in Java?", "class", "struct", "interface", "new", "A", "Java"),
                buildQuestion("Which access modifier allows access within the same package?", "public", "private", "protected", "default", "D", "Java"),

                buildQuestion("Which header file is used for input/output in C?", "stdio.h", "math.h", "string.h", "stdlib.h", "A", "C"),
                buildQuestion("Which operator is used to access the value at a pointer address in C?", "*", "&", "->", "%", "A", "C"),

                buildQuestion("Which C++ feature supports function overloading?", "Inheritance", "Polymorphism", "Encapsulation", "Abstraction", "B", "C++"),
                buildQuestion("What is the correct way to declare a pointer in C++?", "int* p;", "int p*;", "pointer int p;", "*int p;", "A", "C++"),

                buildQuestion("Which SQL command is used to retrieve data from a table?", "GET", "SELECT", "FETCH", "READ", "B", "MySQL"),
                buildQuestion("Which clause is used to filter rows in SQL?", "WHERE", "GROUP BY", "HAVING", "ORDER BY", "A", "MySQL"),

                buildQuestion("Which of the following is a Python immutable data type?", "List", "Dictionary", "Tuple", "Set", "C", "Python"),
                buildQuestion("What is the output of print(len('HELLO')) in Python?", "4", "5", "6", "10", "B", "Python"),

                buildQuestion("Which HTML tag is used to create a hyperlink?", "<link>", "<a>", "<href>", "<url>", "B", "HTML/CSS"),
                buildQuestion("Which CSS property changes the text color?", "font-size", "color", "background", "padding", "B", "HTML/CSS"),

                buildQuestion("Which JavaScript keyword declares a block-scoped variable?", "var", "let", "const", "static", "B", "JavaScript"),
                buildQuestion("What does JSON stand for?", "Java Syntax Object Notation", "JavaScript Object Notation", "Java Standard Output Number", "Java Source Output Network", "B", "JavaScript"),

                buildQuestion("Which library is commonly used for data manipulation in Python?", "NumPy", "Spring", "Node.js", "CSS", "A", "Data Science"),
                buildQuestion("Which metric is used to measure how well a model predicts?", "Latency", "Accuracy", "Bandwidth", "Memory", "B", "Data Science"),

                buildQuestion("Which of the following is a common skill for computer application students?", "Operating System basics", "Database querying", "Web development", "All of the above", "D", "Computer Applications"),
                buildQuestion("Which field is most closely related to developing business software?", "Data Science", "Application Development", "Robotics", "Core Chemistry", "B", "Computer Applications")
        );

        questionRepository.saveAll(questions);
    }

    private Question buildQuestion(String questionText, String optionA, String optionB, String optionC, String optionD,
                                   String correctAnswer, String category) {
        Question question = new Question();
        question.setQuestion(questionText);
        question.setOptionA(optionA);
        question.setOptionB(optionB);
        question.setOptionC(optionC);
        question.setOptionD(optionD);
        question.setCorrectAnswer(correctAnswer);
        question.setCategory(category);
        return question;
    }
}
