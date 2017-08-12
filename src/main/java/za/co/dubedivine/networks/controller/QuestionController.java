package za.co.dubedivine.networks.controller;

import org.springframework.web.bind.annotation.*;
import za.co.dubedivine.networks.model.NewQuestionRequest;
import za.co.dubedivine.networks.model.repository.TagRepository;

//@RestController
//@RequestMapping("q")
public class QuestionController {

    private TagRepository tagRepository;

    public QuestionController(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @PostMapping("new")
    public void addQuestion(@RequestBody NewQuestionRequest newQuestion) {

    }
}
