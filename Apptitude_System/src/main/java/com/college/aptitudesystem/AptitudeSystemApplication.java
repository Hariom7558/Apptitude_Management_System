package com.college.aptitudesystem;

import com.college.aptitudesystem.entity.Question;
import com.college.aptitudesystem.repository.QuestionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class AptitudeSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(AptitudeSystemApplication.class, args);
    }

    @Bean
    public CommandLineRunner seedQuestions(QuestionRepository questionRepository) {
        return args -> {
            if (questionRepository.count() == 0) {
                List<Question> sampleQuestions = List.of(
                        new Question("What is 25 + 17?", "32", "38", "42", "40", "C", "Arithmetic"),
                        new Question("What is 12 x 8?", "96", "84", "92", "88", "A", "Arithmetic"),
                        new Question("Find the next number: 2, 4, 8, 16, ?", "20", "24", "32", "48", "C", "Reasoning"),
                        new Question("If all roses are flowers and some flowers fade quickly, which statement is definitely true?", "All roses fade quickly.", "Some roses fade quickly.", "Some flowers are roses.", "No roses are flowers.", "C", "Logical Reasoning"),
                        new Question("A train travels 60 km in 1 hour. How far in 3 hours?", "120 km", "160 km", "180 km", "200 km", "C", "Arithmetic"),
                        new Question("Which number is divisible by 9?", "43", "54", "47", "56", "B", "Arithmetic"),
                        new Question("Choose the odd one out: Apple, Banana, Carrot, Mango", "Apple", "Banana", "Carrot", "Mango", "C", "General Aptitude"),
                        new Question("If TODAY is coded as UPEZB, how is EARTH coded?", "FBSUI", "FBSVI", "FBTUI", "FBSIY", "A", "Reasoning"),
                        new Question("Which is the capital of France?", "Berlin", "Rome", "Paris", "Madrid", "C", "General Aptitude"),
                        new Question("Which of the following is a prime number?", "21", "29", "33", "39", "B", "Arithmetic")
                );
                questionRepository.saveAll(sampleQuestions);
            }
        };
    }
}
