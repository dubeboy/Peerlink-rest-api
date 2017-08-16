package za.co.dubedivine.networks.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import za.co.dubedivine.networks.model.Answer;
import za.co.dubedivine.networks.model.Comment;
import za.co.dubedivine.networks.model.Question;
import za.co.dubedivine.networks.model.repository.QuestionRepository;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("questions")
public class QuestionsController {

    private final QuestionRepository repository;

    public QuestionsController(QuestionRepository questionRepository) {
        this.repository = questionRepository;
    }

    //TODO: Google post vs put

    @GetMapping
    public List<Question> getAllQuestions() {
        return repository.findAll();
    }

    @PutMapping  //adding anew entity
    public ResponseEntity<Object> addQuestion(@RequestBody Question question) {
        System.out.println("hello there we here bro");
        repository.insert(question);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(question.getId()).toUri();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(uri);
        System.out.println("hello there we here bro 6555555");

        return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
    }

    @PostMapping //for editing
    public void editQuestion(@RequestBody Question question) {
       repository.save(question);
    }

    @PutMapping("/answer/{q_id}")
    public void addAnswer(@PathVariable("q_id") String questionId, @RequestBody Answer answer) {   // could also take in the the whole question
        Question question = repository.findOne(questionId);
        ArrayList<Answer> answers = new ArrayList<>();
        answers.add(answer);
        question.setAnswers(answers);
        repository.save(question);
    }

    @PutMapping("/commentItSS/{q_id}")
    public void addComment(@PathVariable("q_id") String questionId, @RequestBody Comment comment) {
        Question question = repository.findOne(questionId);
        ArrayList<Comment> comments = new ArrayList<>();
        comments.add(comment);
        question.setComments(comments);
        repository.save(question);
    }

    @DeleteMapping("/{q_id}")
    public void deleteQuestion(@PathVariable("q_id") String questionId) {
        //todo: should actually have another collection called deleted stuff where we move this stuff to
        repository.delete(questionId);
    }
}
