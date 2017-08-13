package za.co.dubedivine.networks.controller;

import org.springframework.web.bind.annotation.*;
import za.co.dubedivine.networks.model.Answer;
import za.co.dubedivine.networks.model.Question;
import za.co.dubedivine.networks.model.repository.QuestionRepository;

import java.util.List;

@RestController
@RequestMapping("question")
public class QuestionsController {

    private final QuestionRepository questionRepository;

    public QuestionsController(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    //TODO: Google post vs put

    @GetMapping
    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }


    @PutMapping  //adding anew entity
    public void addQuestion(@RequestBody Question question) {
        questionRepository.insert(question);
        // should respond some how here
    }

    @PostMapping //for editing
    public void editQuestion(@RequestBody Question question) {
       questionRepository.save(question);
    }

    @PutMapping("/answer/{q_id}")
    public void addAnswerToQuestion(@PathVariable("q_id") String questionId, @RequestBody Answer answer) {   // could also take in the the whole question

    }
}
