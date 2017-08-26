package za.co.dubedivine.networks.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.dubedivine.networks.model.Tag;
import za.co.dubedivine.networks.model.User;
import za.co.dubedivine.networks.model.repository.TagRepository;
import za.co.dubedivine.networks.model.repository.UserRepository;
import za.co.dubedivine.networks.model.responseEntity.StatusResponseEntity;

import java.util.List;

/**
 * Created by divine on 2017/08/11.
 */
@RestController
@RequestMapping("/tags")
public class TagController {

    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    public TagController(TagRepository tagRepository,
                         UserRepository userRepository) {
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
    }



    @GetMapping("/all")
    public List<Tag> getAll() {
        List<Tag> tags = this.tagRepository.findAll();
        return tags;
    }

    @PostMapping("/{t_id}/subscribe/{u_id}")
    public ResponseEntity<StatusResponseEntity> subscribeToTag(@PathVariable("t_id") String tagId,
                                                               @PathVariable("u_id") String userId) {

        //function for a user  to listen to a particular TAG
        Tag tag = tagRepository.findOne(tagId);
        User user = userRepository.findOne(userId);
        if (user != null && tag != null) {

            List<Tag> tags = user.getTags();
            tags.add(tag);
            user.setTags(tags);

            userRepository.save(user);
            return
                    new ResponseEntity<>(
                            new StatusResponseEntity(true,
                                    "you now listening on tag"), HttpStatus.CREATED);
        } else {

            return
                    new ResponseEntity<>(
                            new StatusResponseEntity(false,
                                    "you now listening on tag"), HttpStatus.CREATED);
        }
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
        //todo: should also go the tags part and then delete the reference
        tagRepository.delete(id);
    }
}
