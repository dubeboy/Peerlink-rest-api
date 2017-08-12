package za.co.dubedivine.networks.controller;

import org.springframework.web.bind.annotation.*;
import za.co.dubedivine.networks.model.Tag;
import za.co.dubedivine.networks.model.repository.TagRepository;

import java.util.List;

/**
 * Created by divine on 2017/08/11.
 */
@RestController
@RequestMapping("/tags")
public class TagController {

    private TagRepository tagRepository;

    public TagController(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @GetMapping("/all")
    public List<Tag> getAll() {
        List<Tag> tags = this.tagRepository.findAll();
        return tags;
    }

    @PutMapping
    public void insert(@RequestBody Tag tag) {
        this.tagRepository.insert(tag);
    }

    @PostMapping
    public void update(@RequestBody Tag tag) {
        tagRepository.save(tag);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") String id) {
        tagRepository.delete(id);
    }
}
