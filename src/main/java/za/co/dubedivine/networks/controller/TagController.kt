package za.co.dubedivine.networks.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import za.co.dubedivine.networks.model.Tag
import za.co.dubedivine.networks.model.elastic.ElasticTag
import za.co.dubedivine.networks.repository.TagRepository
import za.co.dubedivine.networks.repository.UserRepository
import za.co.dubedivine.networks.model.responseEntity.StatusResponseEntity
import za.co.dubedivine.networks.services.elastic.ElasticTagService
import za.co.dubedivine.networks.util.KUtils

/**
 * Created by divine on 2017/08/11.
 */
@RestController
@RequestMapping("/tags")
class TagController(private val tagRepository: TagRepository,
                    private val userRepository: UserRepository,
                    private val elasticTagService: ElasticTagService) {


    val getAll: List<Tag>
        @GetMapping
        get() = tagRepository.findAll()

    @PostMapping("/{t_id}/subscribe/{u_id}")
    fun subscribeToTag(@PathVariable("t_id") tagId: String,
                       @PathVariable("u_id") userId: String): ResponseEntity<StatusResponseEntity> {

        //function for a user  to listen to a particular TAG
        val tag = tagRepository.findOne(tagId)
        val user = userRepository.findOne(userId)
        if (user != null && tag != null) {

            val tags = user.tags
            tags.add(tag)
            user.tags = tags

            userRepository.save(user)
            return ResponseEntity(
                    StatusResponseEntity(true,
                            "you now listening on tag"), HttpStatus.CREATED)
        } else {

            return ResponseEntity(
                    StatusResponseEntity(false,
                            "you now listening on tag"), HttpStatus.CREATED)
        }
    }


    @PutMapping
    fun insert(@RequestBody tag: Tag) {
        this.tagRepository.insert(tag)
    }

    @PostMapping
    fun update(@RequestBody tag: Tag) {
        tagRepository.save(tag)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable("id") id: String) {
        //todo: should also go the tags part and then delete the reference
        tagRepository.delete(id)
    }

    @GetMapping("/suggest")
    fun suggestTags(@RequestParam("tag") tag: String): List<ElasticTag> {
        println("here is the passed in tag bro $tag")
        return elasticTagService.suggestTag(KUtils.cleanTag(tag))
    }
}
