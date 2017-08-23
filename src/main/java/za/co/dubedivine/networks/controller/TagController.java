package za.co.dubedivine.networks.controller;

import org.springframework.web.bind.annotation.*;
import za.co.dubedivine.networks.model.Question;
import za.co.dubedivine.networks.model.Tag;
import za.co.dubedivine.networks.model.repository.QuestionRepository;

import java.util.List;

/**
 * Created by divine on 2017/08/11.
 */
@RestController
@RequestMapping("/tags")
public class TagController {

    private QuestionRepository tagRepository;

    public TagController(QuestionRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @GetMapping("/all")
    public List<Question> getAll() {
        List<Question> tags = this.tagRepository.findAll();
        return tags;
    }


    @PutMapping
    public void insert(@RequestBody Question tag) {
        this.tagRepository.insert(tag);
    }

    @PostMapping
    public void update(@RequestBody Question tag) {
        tagRepository.save(tag);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") String id) {
        tagRepository.delete(id);
    }
}
